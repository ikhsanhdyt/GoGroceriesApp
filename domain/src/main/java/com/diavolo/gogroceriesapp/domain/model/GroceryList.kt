package com.diavolo.gogroceriesapp.domain.model

data class GroceryList(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)