package com.diavolo.gogroceriesapp.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val aisleOrder: Int
)