package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.repository.GroceryListRepository
import javax.inject.Inject

class UpdateItemUseCase @Inject constructor(
    private val repository: GroceryListRepository
) {
    suspend operator fun invoke(item: GroceryItem) {
        repository.updateItem(item)
    }
}
