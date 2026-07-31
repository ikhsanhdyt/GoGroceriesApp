package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.UnitOfMeasure
import com.diavolo.gogroceriesapp.domain.repository.GroceryListRepository
import javax.inject.Inject

class AddItemUseCase @Inject constructor(
    private val repository: GroceryListRepository
) {
    suspend operator fun invoke(
        listId: Long,
        name: String,
        quantity: Double,
        unit: UnitOfMeasure,
        categoryId: Long? = null,
        estimatedPrice: com.diavolo.gogroceriesapp.core.domain.model.Money? = null,
        note: String? = null
    ): Long {
        val item = GroceryItem(
            listId = listId,
            name = name,
            quantity = quantity,
            unit = unit,
            categoryId = categoryId,
            estimatedPrice = estimatedPrice,
            note = note
        )
        return repository.insertItem(item)
    }
}