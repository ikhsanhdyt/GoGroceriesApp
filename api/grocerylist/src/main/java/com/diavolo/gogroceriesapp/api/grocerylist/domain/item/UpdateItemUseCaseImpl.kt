package com.diavolo.gogroceriesapp.api.grocerylist.domain.item

import com.diavolo.gogroceriesapp.api.grocerylist.data.repository.GroceryListRepository
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryItem
import javax.inject.Inject

class UpdateItemUseCaseImpl @Inject constructor(
    private val repository: GroceryListRepository
) : UpdateItemUseCase {

    override suspend fun invoke(item: GroceryItem) {
        repository.updateItem(item)
    }
}
