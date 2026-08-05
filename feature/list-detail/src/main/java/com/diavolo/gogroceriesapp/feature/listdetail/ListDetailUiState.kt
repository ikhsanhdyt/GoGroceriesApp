package com.diavolo.gogroceriesapp.feature.listdetail

import com.diavolo.gogroceriesapp.domain.Money
import com.diavolo.gogroceriesapp.domain.model.Category
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.GroceryList

data class ListDetailUiState(
    val isLoading: Boolean = true,
    val list: GroceryList? = null,
    val categories: List<Category> = emptyList(),
    val itemGroups: List<GroceryItemGroup> = emptyList(),
    val estimatedTotal: Money = Money.zero(),
    val isAddingItem: Boolean = false,
    val addItemError: String? = null,
    val updatingItemIds: Set<Long> = emptySet(),
    val isNotFound: Boolean = false,
    val errorMessage: String? = null
)

data class GroceryItemGroup(
    val categoryName: String,
    val items: List<GroceryItem>
)
