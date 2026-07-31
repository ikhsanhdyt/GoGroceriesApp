package com.diavolo.gogroceriesapp.core.domain.usecase

import com.diavolo.gogroceriesapp.core.domain.repository.GroceryListRepository
import javax.inject.Inject

class ToggleItemCheckedUseCase @Inject constructor(
    private val repository: GroceryListRepository
) {
    suspend operator fun invoke(itemId: Long, isChecked: Boolean) {
        repository.toggleItemChecked(itemId, isChecked)
    }
}