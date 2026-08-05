package com.diavolo.gogroceriesapp.feature.activeshopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diavolo.gogroceriesapp.domain.model.Category
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.usecase.ComputeEstimatedTotalUseCase
import com.diavolo.gogroceriesapp.domain.usecase.FinishShoppingUseCase
import com.diavolo.gogroceriesapp.domain.usecase.GetCategoriesUseCase
import com.diavolo.gogroceriesapp.domain.usecase.GetListUseCase
import com.diavolo.gogroceriesapp.domain.usecase.ToggleItemCheckedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ActiveShoppingEvent {
    data object ShoppingFinished : ActiveShoppingEvent
    data class Message(val message: String) : ActiveShoppingEvent
}

@HiltViewModel
class ActiveShoppingViewModel @Inject constructor(
    private val getListUseCase: GetListUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val computeEstimatedTotalUseCase: ComputeEstimatedTotalUseCase,
    private val toggleItemCheckedUseCase: ToggleItemCheckedUseCase,
    private val finishShoppingUseCase: FinishShoppingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveShoppingUiState())
    val uiState: StateFlow<ActiveShoppingUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<ActiveShoppingEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private var currentListId: Long? = null
    private var observationJob: Job? = null

    fun loadList(listId: Long) {
        if (currentListId == listId && observationJob?.isActive == true) return

        currentListId = listId
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            _uiState.value = ActiveShoppingUiState(isLoading = true)
            combine(
                getListUseCase(listId),
                getCategoriesUseCase()
            ) { list, categories ->
                if (list == null) {
                    ActiveShoppingUiState(isLoading = false, isNotFound = true)
                } else {
                    ActiveShoppingUiState(
                        isLoading = false,
                        list = list,
                        itemGroups = groupItems(list.items, categories),
                        estimatedTotal = computeEstimatedTotalUseCase(list.items),
                        checkedSubtotal = computeEstimatedTotalUseCase(
                            list.items.filter(GroceryItem::isChecked)
                        ),
                        updatingItemIds = _uiState.value.updatingItemIds,
                        isFinishing = _uiState.value.isFinishing
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
                .collect { state -> _uiState.value = state }
        }
    }

    fun retry() {
        currentListId?.let { listId ->
            currentListId = null
            loadList(listId)
        }
    }

    fun toggleItem(item: GroceryItem) {
        if (item.id in _uiState.value.updatingItemIds) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                updatingItemIds = _uiState.value.updatingItemIds + item.id
            )
            runCatching {
                toggleItemCheckedUseCase(item.id, !item.isChecked)
            }.onFailure {
                eventChannel.send(
                    ActiveShoppingEvent.Message(
                        "Couldn't update ${item.name}. Please try again."
                    )
                )
            }
            _uiState.value = _uiState.value.copy(
                updatingItemIds = _uiState.value.updatingItemIds - item.id
            )
        }
    }

    fun finishShopping() {
        val list = _uiState.value.list ?: return
        if (_uiState.value.isFinishing) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFinishing = true)
            runCatching {
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
            _uiState.value = _uiState.value.copy(isFinishing = false)
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

    private data class GroupWithOrder(
        val group: ShoppingItemGroup,
        val aisleOrder: Int
    )
}
