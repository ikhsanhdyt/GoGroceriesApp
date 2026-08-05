package com.diavolo.gogroceriesapp.feature.activeshopping

import com.diavolo.gogroceriesapp.domain.Money
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.GroceryList

data class ActiveShoppingUiState(
    val isLoading: Boolean = true,
    val list: GroceryList? = null,
    val itemGroups: List<ShoppingItemGroup> = emptyList(),
    val estimatedTotal: Money = Money.zero(),
    val checkedSubtotal: Money = Money.zero(),
    val updatingItemIds: Set<Long> = emptySet(),
    val isFinishing: Boolean = false,
    val isNotFound: Boolean = false,
    val errorMessage: String? = null
)

data class ShoppingItemGroup(
    val categoryName: String,
    val items: List<GroceryItem>
)
