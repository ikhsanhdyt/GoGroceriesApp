package com.diavolo.gogroceriesapp.api.grocerylist.domain.list

import com.diavolo.gogroceriesapp.api.grocerylist.data.repository.GroceryListRepository
import javax.inject.Inject

class CreateListUseCaseImpl @Inject constructor(
    private val repository: GroceryListRepository
) : CreateListUseCase {

    override suspend fun invoke(name: String, budgetRupiah: Long?): Long {
        return repository.create(name = name, budgetRupiah = budgetRupiah)
    }
}
