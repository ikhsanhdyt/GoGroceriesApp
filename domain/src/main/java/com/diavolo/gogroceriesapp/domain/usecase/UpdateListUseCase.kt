package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.repository.GroceryListRepository
import javax.inject.Inject

class UpdateListUseCase @Inject constructor(
    private val repository: GroceryListRepository
) {
    suspend operator fun invoke(list: GroceryList) {
        repository.updateList(list)
    }
}