package com.diavolo.gogroceriesapp.api.grocerylist.domain.item

import com.diavolo.gogroceriesapp.api.grocerylist.data.repository.GroceryListRepository
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryItem
import javax.inject.Inject

class AddItemUseCaseImpl @Inject constructor(
    private val repository: GroceryListRepository
) : AddItemUseCase {

    override suspend fun invoke(item: GroceryItem): Long {
        return repository.addItem(item)
    }
}
