package com.diavolo.gogroceriesapp.feature.listdetail.viewmodel

import androidx.lifecycle.viewModelScope
import com.diavolo.gogroceriesapp.api.category.domain.get.GetCategoriesUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.calculation.ComputeEstimatedTotalUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.get.GetListUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.item.AddItemUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.item.DeleteItemUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.item.ToggleItemCheckedUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.item.UpdateItemUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.shopping.StartShoppingUseCase
import com.diavolo.gogroceriesapp.core.base.BaseState
import com.diavolo.gogroceriesapp.core.base.BaseViewModel
import com.diavolo.gogroceriesapp.core.base.asBaseState
import com.diavolo.gogroceriesapp.core.entity.category.Category
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryItem
import com.diavolo.gogroceriesapp.core.entity.grocery.UnitOfMeasure
import com.diavolo.gogroceriesapp.feature.listdetail.state.GroceryItemGroup
import com.diavolo.gogroceriesapp.feature.listdetail.state.ListDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ListDetailEvent {
    data object ItemAdded : ListDetailEvent
    data object ItemUpdated : ListDetailEvent
    data object ItemDeleted : ListDetailEvent
    data class ShoppingStarted(val listId: Long) : ListDetailEvent
    data class Message(val message: String) : ListDetailEvent
}

@HiltViewModel
class ListDetailViewModel @Inject constructor(
    private val getListUseCase: GetListUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val computeEstimatedTotal: ComputeEstimatedTotalUseCase,
    private val addItemUseCase: AddItemUseCase,
    private val toggleItemCheckedUseCase: ToggleItemCheckedUseCase,
    private val updateItemUseCase: UpdateItemUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
    private val startShoppingUseCase: StartShoppingUseCase
) : BaseViewModel() {

    /**
     * Screen content coming from the database. Only the data fields of [ListDetailUiState] are
     * used here; the operation flags live in [operationState].
     */
    private val dataState = MutableStateFlow(ListDetailUiState())

    /**
     * In-flight operations and their errors. Every writer uses [MutableStateFlow.update], and the
     * database stream never reads or copies these values, so neither can overwrite the other.
     */
    private val operationState = MutableStateFlow(OperationState())

    val uiState: StateFlow<ListDetailUiState> = combine(dataState, operationState) { data, operation ->
        data.copy(
            isAddingItem = operation.isAddingItem,
            addItemError = operation.addItemError,
            editItemError = operation.editItemError,
            updatingItemIds = operation.updatingItemIds,
            isStartingShopping = operation.isStartingShopping
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ListDetailUiState())

    private val eventChannel = Channel<ListDetailEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private var currentListId: Long? = null
    private var observationJob: Job? = null

    fun loadList(listId: Long) {
        if (currentListId == listId && observationJob?.isActive == true) return

        currentListId = listId
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            combine(
                getListUseCase(listId),
                getCategoriesUseCase()
            ) { list, categories ->
                if (list == null) {
                    ListDetailUiState(isLoading = false, isNotFound = true)
                } else {
                    ListDetailUiState(
                        isLoading = false,
                        list = list,
                        categories = categories,
                        itemGroups = groupItems(list.items, categories),
                        estimatedTotal = computeEstimatedTotal(list.items)
                    )
                }
            }
                .asBaseState()
                .collect { state ->
                    dataState.value = when (state) {
                        BaseState.StateInitial, BaseState.StateLoading -> {
                            ListDetailUiState(isLoading = true)
                        }
                        is BaseState.StateFailed -> ListDetailUiState(
                            isLoading = false,
                            errorMessage = "Couldn't load this shopping list."
                        )
                        is BaseState.StateSuccess -> state.data
                    }
                }
        }
    }

    fun retry() {
        currentListId?.let { listId ->
            currentListId = null
            loadList(listId)
        }
    }

    fun clearAddItemError() {
        operationState.update { it.copy(addItemError = null) }
    }

    fun clearEditItemError() {
        operationState.update { it.copy(editItemError = null) }
    }

    fun addItem(
        name: String,
        quantity: Double,
        unit: UnitOfMeasure,
        categoryId: Long?,
        estimatedPriceRupiah: Long?
    ) {
        val list = dataState.value.list ?: return
        if (name.isBlank() || quantity <= 0 || operationState.value.isAddingItem) return

        operationState.update { it.copy(isAddingItem = true, addItemError = null) }
        val nextPosition = (list.items.maxOfOrNull(GroceryItem::position) ?: -1) + 1
        launchSafeJob(
            block = {
                addItemUseCase(
                    GroceryItem(
                        listId = list.id,
                        categoryId = categoryId,
                        name = name.trim(),
                        quantity = quantity,
                        unit = unit,
                        estimatedPriceRupiah = estimatedPriceRupiah,
                        actualPriceRupiah = null,
                        isChecked = false,
                        notes = null,
                        position = nextPosition
                    )
                )
            },
            onSuccess = {
                operationState.update { it.copy(isAddingItem = false) }
                eventChannel.send(ListDetailEvent.ItemAdded)
            },
            onError = {
                operationState.update {
                    it.copy(
                        isAddingItem = false,
                        addItemError = "Couldn't add this item. Please try again."
                    )
                }
            }
        )
    }

    fun toggleItem(item: GroceryItem) {
        if (item.id in operationState.value.updatingItemIds) return

        operationState.update { it.copy(updatingItemIds = it.updatingItemIds + item.id) }
        launchSafeJob(
            block = { toggleItemCheckedUseCase(item.id, !item.isChecked) },
            onError = {
                eventChannel.send(
                    ListDetailEvent.Message("Couldn't update ${item.name}. Please try again.")
                )
            },
            onFinally = {
                operationState.update { it.copy(updatingItemIds = it.updatingItemIds - item.id) }
            }
        )
    }

    fun updateItem(
        item: GroceryItem,
        name: String,
        quantity: Double,
        unit: UnitOfMeasure,
        categoryId: Long?,
        estimatedPriceRupiah: Long?
    ) {
        if (
            name.isBlank() ||
            quantity <= 0 ||
            item.id in operationState.value.updatingItemIds
        ) return

        operationState.update {
            it.copy(
                editItemError = null,
                updatingItemIds = it.updatingItemIds + item.id
            )
        }
        launchSafeJob(
            block = {
                updateItemUseCase(
                    item.copy(
                        name = name.trim(),
                        quantity = quantity,
                        unit = unit,
                        categoryId = categoryId,
                        estimatedPriceRupiah = estimatedPriceRupiah
                    )
                )
            },
            onSuccess = { eventChannel.send(ListDetailEvent.ItemUpdated) },
            onError = {
                operationState.update {
                    it.copy(editItemError = "Couldn't save your changes. Please try again.")
                }
            },
            onFinally = {
                operationState.update { it.copy(updatingItemIds = it.updatingItemIds - item.id) }
            }
        )
    }

    fun deleteItem(item: GroceryItem) {
        if (item.id in operationState.value.updatingItemIds) return

        operationState.update { it.copy(updatingItemIds = it.updatingItemIds + item.id) }
        launchSafeJob(
            block = { deleteItemUseCase(item.id) },
            onSuccess = { eventChannel.send(ListDetailEvent.ItemDeleted) },
            onError = {
                eventChannel.send(
                    ListDetailEvent.Message("Couldn't delete ${item.name}. Please try again.")
                )
            },
            onFinally = {
                operationState.update { it.copy(updatingItemIds = it.updatingItemIds - item.id) }
            }
        )
    }

    fun startShopping() {
        val list = dataState.value.list ?: return
        if (operationState.value.isStartingShopping) return

        operationState.update { it.copy(isStartingShopping = true) }
        launchSafeJob(
            block = { startShoppingUseCase(list) },
            onSuccess = { eventChannel.send(ListDetailEvent.ShoppingStarted(list.id)) },
            onError = {
                eventChannel.send(
                    ListDetailEvent.Message(
                        it.message ?: "Couldn't start shopping. Please try again."
                    )
                )
            },
            onFinally = { operationState.update { it.copy(isStartingShopping = false) } }
        )
    }

    private fun groupItems(
        items: List<GroceryItem>,
        categories: List<Category>
    ): List<GroceryItemGroup> {
        val categoryById = categories.associateBy(Category::id)
        return items
            .groupBy(GroceryItem::categoryId)
            .map { (categoryId, groupedItems) ->
                val category = categoryId?.let(categoryById::get)
                GroupWithOrder(
                    group = GroceryItemGroup(
                        categoryName = category?.name ?: "Other",
                        items = groupedItems.sortedBy(GroceryItem::position)
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
        val isAddingItem: Boolean = false,
        val addItemError: String? = null,
        val editItemError: String? = null,
        val updatingItemIds: Set<Long> = emptySet(),
        val isStartingShopping: Boolean = false
    )

    private data class GroupWithOrder(
        val group: GroceryItemGroup,
        val aisleOrder: Int
    )
}
