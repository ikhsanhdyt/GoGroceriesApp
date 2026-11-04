package com.diavolo.gogroceriesapp.feature.listdetail.state

import com.diavolo.gogroceriesapp.core.entity.category.Category
import com.diavolo.gogroceriesapp.core.entity.common.Money
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryItem
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryList

data class ListDetailUiState(
    val isLoading: Boolean = true,
    val list: GroceryList? = null,
    val categories: List<Category> = emptyList(),
    val itemGroups: List<GroceryItemGroup> = emptyList(),
    val estimatedTotal: Money = Money.zero(),
    val isAddingItem: Boolean = false,
    val addItemError: String? = null,
    val editItemError: String? = null,
    val updatingItemIds: Set<Long> = emptySet(),
    val isStartingShopping: Boolean = false,
    val isNotFound: Boolean = false,
    val errorMessage: String? = null
)

data class GroceryItemGroup(
    val categoryName: String,
    val items: List<GroceryItem>
)
