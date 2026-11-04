package com.diavolo.gogroceriesapp.api.grocerylist.domain.list

import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryList

interface UpdateListUseCase {
    suspend operator fun invoke(list: GroceryList)
}
