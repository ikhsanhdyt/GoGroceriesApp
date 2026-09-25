package com.diavolo.gogroceriesapp.feature.activeshopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diavolo.gogroceriesapp.core.util.suspendRunCatching
import com.diavolo.gogroceriesapp.domain.model.Category
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.usecase.ComputeActualTotalUseCase
import com.diavolo.gogroceriesapp.domain.usecase.ComputeEstimatedTotalUseCase
import com.diavolo.gogroceriesapp.domain.usecase.FinishShoppingUseCase
import com.diavolo.gogroceriesapp.domain.usecase.GetCategoriesUseCase
import com.diavolo.gogroceriesapp.domain.usecase.GetListUseCase
import com.diavolo.gogroceriesapp.domain.usecase.ToggleItemCheckedUseCase
import com.diavolo.gogroceriesapp.domain.usecase.UpdateItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ActiveShoppingEvent {
    data object ShoppingFinished : ActiveShoppingEvent
    data object PriceUpdated : ActiveShoppingEvent
    data class Message(val message: String) : ActiveShoppingEvent
}

@HiltViewModel
class ActiveShoppingViewModel @Inject constructor(
    private val getListUseCase: GetListUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val computeEstimatedTotalUseCase: ComputeEstimatedTotalUseCase,
    private val computeActualTotalUseCase: ComputeActualTotalUseCase,
    private val toggleItemCheckedUseCase: ToggleItemCheckedUseCase,
    private val finishShoppingUseCase: FinishShoppingUseCase,
    private val updateItemUseCase: UpdateItemUseCase
) : ViewModel() {

    /**
     * Screen content coming from the database. Only the data fields of [ActiveShoppingUiState]
     * are used here; the operation flags live in [operationState].
     *
     * Every write to the database re-emits this stream, so it must never read or copy the
     * operation flags: a stale copy would overwrite a flag that changed in the meantime.
     */
    private val dataState = MutableStateFlow(ActiveShoppingUiState())

    /** In-flight operations and their errors. Writers use [MutableStateFlow.update]. */
    private val operationState = MutableStateFlow(OperationState())

    val uiState: StateFlow<ActiveShoppingUiState> =
        combine(dataState, operationState) { data, operation ->
            data.copy(
                priceUpdateError = operation.priceUpdateError,
                updatingItemIds = operation.updatingItemIds,
                isFinishing = operation.isFinishing
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, ActiveShoppingUiState())

    private val eventChannel = Channel<ActiveShoppingEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private var currentListId: Long? = null
    private var observationJob: Job? = null

    fun loadList(listId: Long) {
        if (currentListId == listId && observationJob?.isActive == true) return

        currentListId = listId
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            dataState.value = ActiveShoppingUiState(isLoading = true)
            combine(
                getListUseCase(listId),
                getCategoriesUseCase()
            ) { list, categories ->
                if (list == null) {
                    ActiveShoppingUiState(isLoading = false, isNotFound = true)
                } else {
                    val checkedItems = list.items.filter(GroceryItem::isChecked)
                    ActiveShoppingUiState(
                        isLoading = false,
                        list = list,
                        itemGroups = groupItems(list.items, categories),
                        estimatedTotal = computeEstimatedTotalUseCase(list.items),
                        actualCheckedSubtotal = computeActualTotalUseCase(checkedItems),
                        checkedItemsMissingPrice = checkedItems.count {
                            it.actualPriceRupiah == null
                        }
                    )
                }
            }
                .catch {
                    emit(
                        ActiveShoppingUiState(
                            isLoading = false,
                            errorMessage = "Couldn't load shopping mode."
                        )
                    )
                }
                .collect { state -> dataState.value = state }
        }
    }

    fun retry() {
        currentListId?.let { listId ->
            currentListId = null
            loadList(listId)
        }
    }

    fun clearPriceUpdateError() {
        operationState.update { it.copy(priceUpdateError = null) }
    }

    fun toggleItem(item: GroceryItem) {
        if (item.id in operationState.value.updatingItemIds) return

        viewModelScope.launch {
            operationState.update { it.copy(updatingItemIds = it.updatingItemIds + item.id) }
            suspendRunCatching {
                toggleItemCheckedUseCase(item.id, !item.isChecked)
            }.onFailure {
                eventChannel.send(
                    ActiveShoppingEvent.Message(
                        "Couldn't update ${item.name}. Please try again."
                    )
                )
            }
            operationState.update { it.copy(updatingItemIds = it.updatingItemIds - item.id) }
        }
    }

    fun updateActualPrice(item: GroceryItem, actualPriceRupiah: Long?) {
        if (item.id in operationState.value.updatingItemIds) return

        viewModelScope.launch {
            operationState.update {
                it.copy(
                    priceUpdateError = null,
                    updatingItemIds = it.updatingItemIds + item.id
                )
            }
            suspendRunCatching {
                updateItemUseCase(item.copy(actualPriceRupiah = actualPriceRupiah))
            }.onSuccess {
                eventChannel.send(ActiveShoppingEvent.PriceUpdated)
            }.onFailure {
                operationState.update {
                    it.copy(priceUpdateError = "Couldn't save the actual price. Please try again.")
                }
            }
            operationState.update { it.copy(updatingItemIds = it.updatingItemIds - item.id) }
        }
    }

    fun finishShopping() {
        val list = dataState.value.list ?: return
        if (operationState.value.isFinishing) return

        viewModelScope.launch {
            operationState.update { it.copy(isFinishing = true) }
            suspendRunCatching {
                finishShoppingUseCase(list)
            }.onSuccess {
                eventChannel.send(ActiveShoppingEvent.ShoppingFinished)
            }.onFailure {
                eventChannel.send(
                    ActiveShoppingEvent.Message(
                        it.message ?: "Couldn't finish shopping. Please try again."
                    )
                )
            }
            operationState.update { it.copy(isFinishing = false) }
        }
    }

    private fun groupItems(
        items: List<GroceryItem>,
        categories: List<Category>
    ): List<ShoppingItemGroup> {
        val categoryById = categories.associateBy(Category::id)
        return items
            .groupBy(GroceryItem::categoryId)
            .map { (categoryId, groupedItems) ->
                val category = categoryId?.let(categoryById::get)
                GroupWithOrder(
                    group = ShoppingItemGroup(
                        categoryName = category?.name ?: "Other",
                        items = groupedItems.sortedWith(
                            compareBy<GroceryItem> { it.isChecked }
                                .thenBy(GroceryItem::position)
                        )
                    ),
                    aisleOrder = category?.aisleOrder ?: Int.MAX_VALUE
                )
            }
            .sortedWith(
                compareBy<GroupWithOrder> { it.aisleOrder }
                    .thenBy { it.group.categoryName }
            )
            .map(GroupWithOrder::group)
    }

    private data class OperationState(
        val priceUpdateError: String? = null,
        val updatingItemIds: Set<Long> = emptySet(),
        val isFinishing: Boolean = false
    )

    private data class GroupWithOrder(
        val group: ShoppingItemGroup,
        val aisleOrder: Int
    )
}
