package com.diavolo.gogroceriesapp.data.local.entity.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.diavolo.gogroceriesapp.data.local.entity.GroceryItemEntity
import com.diavolo.gogroceriesapp.data.local.entity.GroceryListEntity

data class ListWithItems(
    @Embedded val list: GroceryListEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "listId"
    )
    val items: List<GroceryItemEntity>
)