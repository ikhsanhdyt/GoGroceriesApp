package com.diavolo.gogroceriesapp.domain.model

data class GroceryList(
    val id: Long = 0,
    val name: String,
    val status: ListStatus,
    val budgetCents: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val items: List<GroceryItem> = emptyList()
)