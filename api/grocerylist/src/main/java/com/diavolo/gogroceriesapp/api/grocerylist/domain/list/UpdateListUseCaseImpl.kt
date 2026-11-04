package com.diavolo.gogroceriesapp.api.grocerylist.domain.list

import com.diavolo.gogroceriesapp.api.grocerylist.data.repository.GroceryListRepository
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryList
import javax.inject.Inject

class UpdateListUseCaseImpl @Inject constructor(
    private val repository: GroceryListRepository
) : UpdateListUseCase {

    override suspend fun invoke(list: GroceryList) {
        repository.update(list)
    }
}
