package com.diavolo.gogroceriesapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.ForeignKey.Companion.SET_NULL
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "grocery_items",
    foreignKeys = [
        ForeignKey(
            entity = GroceryListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = SET_NULL
        )
    ],
    indices = [Index("listId"), Index("categoryId")]
)
data class GroceryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    val categoryId: Long?,
    val name: String,
    val quantity: Double,
    val unit: String,
    val estimatedPriceRupiah: Long?,
    val actualPriceRupiah: Long?,
    val isChecked: Boolean,
    val notes: String?,
    val position: Int
)
