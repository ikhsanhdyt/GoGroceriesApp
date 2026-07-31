package com.diavolo.gogroceriesapp.core.domain.usecase

import com.diavolo.gogroceriesapp.core.domain.model.GroceryList
import com.diavolo.gogroceriesapp.core.domain.repository.GroceryListRepository
import javax.inject.Inject

class DeleteListUseCase @Inject constructor(
    private val repository: GroceryListRepository
) {
    suspend operator fun invoke(list: GroceryList) {
        repository.deleteList(list)
    }
}