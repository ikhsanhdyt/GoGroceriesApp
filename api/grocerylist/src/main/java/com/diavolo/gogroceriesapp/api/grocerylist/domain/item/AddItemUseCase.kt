package com.diavolo.gogroceriesapp.api.grocerylist.domain.item

import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryItem

interface AddItemUseCase {
    suspend operator fun invoke(item: GroceryItem): Long
}
