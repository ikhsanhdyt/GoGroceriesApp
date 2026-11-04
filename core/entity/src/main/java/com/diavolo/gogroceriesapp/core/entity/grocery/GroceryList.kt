package com.diavolo.gogroceriesapp.core.entity.grocery

data class GroceryList(
    val id: Long = 0,
    val name: String,
    val status: ListStatus,
    val budgetRupiah: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val items: List<GroceryItem> = emptyList(),
    val completedAt: Long? = null
)
