package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.repository.GroceryListRepository
import javax.inject.Inject

class CreateListUseCase @Inject constructor(
    private val repository: GroceryListRepository
) {
    suspend operator fun invoke(name: String): Long {
        val list = GroceryList(name = name)
        return repository.insertList(list)
    }
}