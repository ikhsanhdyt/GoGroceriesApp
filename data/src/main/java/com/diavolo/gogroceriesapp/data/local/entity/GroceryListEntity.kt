package com.diavolo.gogroceriesapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grocery_lists")
data class GroceryListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val status: String,
    val budgetCents: Long?,
    val createdAt: Long,
    val updatedAt: Long
)