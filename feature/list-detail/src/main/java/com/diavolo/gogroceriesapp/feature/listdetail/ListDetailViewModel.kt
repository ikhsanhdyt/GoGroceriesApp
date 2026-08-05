package com.diavolo.gogroceriesapp.feature.listdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diavolo.gogroceriesapp.domain.model.Category
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.usecase.ComputeEstimatedTotalUseCase
import com.diavolo.gogroceriesapp.domain.usecase.GetCategoriesUseCase
import com.diavolo.gogroceriesapp.domain.usecase.GetListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListDetailViewModel @Inject constructor(
    private val getListUseCase: GetListUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val computeEstimatedTotal: ComputeEstimatedTotalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListDetailUiState())
    val uiState: StateFlow<ListDetailUiState> = _uiState.asStateFlow()

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
                        itemGroups = groupItems(list.items, categories),
                        estimatedTotal = computeEstimatedTotal(list.items)
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
