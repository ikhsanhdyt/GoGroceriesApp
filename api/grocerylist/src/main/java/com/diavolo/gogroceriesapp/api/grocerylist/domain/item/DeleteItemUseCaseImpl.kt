package com.diavolo.gogroceriesapp.api.grocerylist.domain.item

import com.diavolo.gogroceriesapp.api.grocerylist.data.repository.GroceryListRepository
import javax.inject.Inject

class DeleteItemUseCaseImpl @Inject constructor(
    private val repository: GroceryListRepository
) : DeleteItemUseCase {

    override suspend fun invoke(itemId: Long) {
        repository.deleteItem(itemId)
    }
}
