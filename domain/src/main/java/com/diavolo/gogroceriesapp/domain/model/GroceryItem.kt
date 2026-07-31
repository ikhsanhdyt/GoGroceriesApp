package com.diavolo.gogroceriesapp.domain.model

import com.diavolo.gogroceriesapp.domain.Money

data class GroceryItem(
    val id: Long = 0,
    val listId: Long,
    val categoryId: Long?,
    val name: String,
    val quantity: Double,
    val unit: UnitOfMeasure,
    val estimatedPriceCents: Long?,
    val actualPriceCents: Long?,
    val isChecked: Boolean,
    val notes: String?,
    val position: Int
)