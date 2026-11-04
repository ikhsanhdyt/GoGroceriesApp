package com.diavolo.gogroceriesapp.api.grocerylist.domain.item

import com.diavolo.gogroceriesapp.api.grocerylist.data.repository.GroceryListRepository
import javax.inject.Inject

class ToggleItemCheckedUseCaseImpl @Inject constructor(
    private val repository: GroceryListRepository
) : ToggleItemCheckedUseCase {

    override suspend fun invoke(itemId: Long, isChecked: Boolean) {
        repository.toggleChecked(itemId = itemId, checked = isChecked)
    }
}
