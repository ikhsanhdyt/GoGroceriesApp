package com.diavolo.gogroceriesapp.core.domain.usecase

import com.diavolo.gogroceriesapp.core.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.core.domain.repository.GroceryListRepository
import javax.inject.Inject

class AddItemUseCase @Inject constructor(
    private val repository: GroceryListRepository
) {
    suspend operator fun invoke(
        listId: Long,
        name: String,
        quantity: Double,
        unit: com.diavolo.gogroceriesapp.core.domain.model.UnitOfMeasure,
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