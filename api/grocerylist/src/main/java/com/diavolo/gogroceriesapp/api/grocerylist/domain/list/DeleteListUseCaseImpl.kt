package com.diavolo.gogroceriesapp.api.grocerylist.domain.list

import com.diavolo.gogroceriesapp.api.grocerylist.data.repository.GroceryListRepository
import javax.inject.Inject

class DeleteListUseCaseImpl @Inject constructor(
    private val repository: GroceryListRepository
) : DeleteListUseCase {

    override suspend fun invoke(id: Long) {
        repository.delete(id)
    }
}
