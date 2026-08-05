package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.repository.GroceryListRepository
import javax.inject.Inject

class DeleteItemUseCase @Inject constructor(
    private val repository: GroceryListRepository
) {
    suspend operator fun invoke(itemId: Long) {
        repository.deleteItem(itemId)
    }
}
