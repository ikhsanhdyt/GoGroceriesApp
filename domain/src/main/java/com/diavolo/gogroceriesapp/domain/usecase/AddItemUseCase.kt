package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.Money
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.UnitOfMeasure
import com.diavolo.gogroceriesapp.domain.repository.GroceryListRepository
import javax.inject.Inject

class AddItemUseCase @Inject constructor(
    private val repository: GroceryListRepository
) {
    suspend operator fun invoke(item: GroceryItem): Long {
        return repository.addItem(item)
    }
}