package com.diavolo.gogroceriesapp.domain.model

data class GroceryItem(
    val id: Long = 0,
    val listId: Long,
    val name: String,
    val quantity: Double,
    val unit: UnitOfMeasure,
    val categoryId: Long? = null,
    val estimatedPrice: Money? = null,
    val isChecked: Boolean = false,
    val note: String? = null
)