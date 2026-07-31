package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.repository.GroceryListRepository
import javax.inject.Inject

class CreateListUseCase @Inject constructor(
    private val repository: GroceryListRepository
) {
    suspend operator fun invoke(name: String, budgetCents: Long? = null): Long {
        return repository.create(name = name, budgetCents = budgetCents)
    }
}