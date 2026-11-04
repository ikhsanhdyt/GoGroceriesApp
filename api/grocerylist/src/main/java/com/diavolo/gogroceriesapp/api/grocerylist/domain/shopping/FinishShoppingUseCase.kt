package com.diavolo.gogroceriesapp.api.grocerylist.domain.shopping

import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryList

interface FinishShoppingUseCase {
    suspend operator fun invoke(list: GroceryList)
}
