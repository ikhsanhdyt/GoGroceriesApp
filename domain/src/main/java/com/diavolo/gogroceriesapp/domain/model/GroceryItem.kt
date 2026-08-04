package com.diavolo.gogroceriesapp.domain.model

data class GroceryItem(
    val id: Long = 0,
    val listId: Long,
    val categoryId: Long?,
    val name: String,
    val quantity: Double,
    val unit: UnitOfMeasure,
    val estimatedPriceRupiah: Long?,
    val actualPriceRupiah: Long?,
    val isChecked: Boolean,
    val notes: String?,
    val position: Int
)
