package com.diavolo.gogroceriesapp.core.entity.category

data class Category(
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val aisleOrder: Int
)