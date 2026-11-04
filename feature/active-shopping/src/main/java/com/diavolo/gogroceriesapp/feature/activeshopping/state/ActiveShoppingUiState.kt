package com.diavolo.gogroceriesapp.feature.activeshopping.state

import com.diavolo.gogroceriesapp.core.entity.common.Money
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryItem
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryList

data class ActiveShoppingUiState(
    val isLoading: Boolean = true,
    val list: GroceryList? = null,
    val itemGroups: List<ShoppingItemGroup> = emptyList(),
    val estimatedTotal: Money = Money.zero(),
    val actualCheckedSubtotal: Money = Money.zero(),
    val checkedItemsMissingPrice: Int = 0,
    val priceUpdateError: String? = null,
    val updatingItemIds: Set<Long> = emptySet(),
    val isFinishing: Boolean = false,
    val isNotFound: Boolean = false,
    val errorMessage: String? = null
)

data class ShoppingItemGroup(
    val categoryName: String,
    val items: List<GroceryItem>
)
