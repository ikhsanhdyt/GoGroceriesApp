package com.diavolo.gogroceriesapp.api.grocerylist.domain.item

import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryItem

interface UpdateItemUseCase {
    suspend operator fun invoke(item: GroceryItem)
}
