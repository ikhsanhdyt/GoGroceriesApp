package com.diavolo.gogroceriesapp.api.grocerylist.domain.item

interface ToggleItemCheckedUseCase {
    suspend operator fun invoke(itemId: Long, isChecked: Boolean)
}
