package com.diavolo.gogroceriesapp.feature.listdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diavolo.gogroceriesapp.domain.model.Category
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.UnitOfMeasure
import com.diavolo.gogroceriesapp.domain.usecase.AddItemUseCase
import com.diavolo.gogroceriesapp.domain.usecase.ComputeEstimatedTotalUseCase
import com.diavolo.gogroceriesapp.domain.usecase.DeleteItemUseCase
import com.diavolo.gogroceriesapp.domain.usecase.GetCategoriesUseCase
import com.diavolo.gogroceriesapp.domain.usecase.GetListUseCase
import com.diavolo.gogroceriesapp.domain.usecase.ToggleItemCheckedUseCase
import com.diavolo.gogroceriesapp.domain.usecase.UpdateItemUseCase
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

sealed interface ListDetailEvent {
    data object ItemAdded : ListDetailEvent
    data object ItemUpdated : ListDetailEvent
    data object ItemDeleted : ListDetailEvent
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
    private val deleteItemUseCase: DeleteItemUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListDetailUiState())
    val uiState: StateFlow<ListDetailUiState> = _uiState.asStateFlow()
    private val eventChannel = Channel<ListDetailEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private var currentListId: Long? = null
    private var observationJob: Job? = null

    fun loadList(listId: Long) {
        if (currentListId == listId && observationJob?.isActive == true) return

        currentListId = listId
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            _uiState.value = ListDetailUiState(isLoading = true)
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
                        estimatedTotal = computeEstimatedTotal(list.items),
                        isAddingItem = _uiState.value.isAddingItem,
                        addItemError = _uiState.value.addItemError,
                        editItemError = _uiState.value.editItemError,
                        updatingItemIds = _uiState.value.updatingItemIds
                    )
                }
            }
                .catch {
                    emit(
                        ListDetailUiState(
                            isLoading = false,
                            errorMessage = "Couldn't load this shopping list."
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

    fun clearAddItemError() {
        _uiState.value = _uiState.value.copy(addItemError = null)
    }

    fun clearEditItemError() {
        _uiState.value = _uiState.value.copy(editItemError = null)
    }

    fun addItem(
        name: String,
        quantity: Double,
        unit: UnitOfMeasure,
        categoryId: Long?,
        estimatedPriceRupiah: Long?
    ) {
        val list = _uiState.value.list ?: return
        if (name.isBlank() || quantity <= 0 || _uiState.value.isAddingItem) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAddingItem = true,
                addItemError = null
            )
            val nextPosition = (list.items.maxOfOrNull(GroceryItem::position) ?: -1) + 1
            runCatching {
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
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isAddingItem = false)
                eventChannel.send(ListDetailEvent.ItemAdded)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isAddingItem = false,
                    addItemError = "Couldn't add this item. Please try again."
                )
            }
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
                    ListDetailEvent.Message(
                        "Couldn't update ${item.name}. Please try again."
                    )
                )
            }
            _uiState.value = _uiState.value.copy(
                updatingItemIds = _uiState.value.updatingItemIds - item.id
            )
        }
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
            item.id in _uiState.value.updatingItemIds
        ) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                editItemError = null,
                updatingItemIds = _uiState.value.updatingItemIds + item.id
            )
            runCatching {
                updateItemUseCase(
                    item.copy(
                        name = name.trim(),
                        quantity = quantity,
                        unit = unit,
                        categoryId = categoryId,
                        estimatedPriceRupiah = estimatedPriceRupiah
                    )
                )
            }.onSuccess {
                eventChannel.send(ListDetailEvent.ItemUpdated)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    editItemError = "Couldn't save your changes. Please try again."
                )
            }
            _uiState.value = _uiState.value.copy(
                updatingItemIds = _uiState.value.updatingItemIds - item.id
            )
        }
    }

    fun deleteItem(item: GroceryItem) {
        if (item.id in _uiState.value.updatingItemIds) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                updatingItemIds = _uiState.value.updatingItemIds + item.id
            )
            runCatching {
                deleteItemUseCase(item.id)
            }.onSuccess {
                eventChannel.send(ListDetailEvent.ItemDeleted)
            }.onFailure {
                eventChannel.send(
                    ListDetailEvent.Message(
                        "Couldn't delete ${item.name}. Please try again."
                    )
                )
            }
            _uiState.value = _uiState.value.copy(
                updatingItemIds = _uiState.value.updatingItemIds - item.id
            )
        }
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

    private data class GroupWithOrder(
        val group: GroceryItemGroup,
        val aisleOrder: Int
    )
}
