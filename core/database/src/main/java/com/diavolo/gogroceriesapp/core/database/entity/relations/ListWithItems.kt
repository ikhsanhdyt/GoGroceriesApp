package com.diavolo.gogroceriesapp.core.database.entity.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.diavolo.gogroceriesapp.core.database.entity.GroceryItemEntity
import com.diavolo.gogroceriesapp.core.database.entity.GroceryListEntity

data class ListWithItems(
    @Embedded val list: GroceryListEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "listId"
    )
    val items: List<GroceryItemEntity>
)