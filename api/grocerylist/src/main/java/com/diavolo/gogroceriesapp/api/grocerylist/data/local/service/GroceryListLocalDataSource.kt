package com.diavolo.gogroceriesapp.api.grocerylist.data.local.service

import com.diavolo.gogroceriesapp.core.database.entity.GroceryItemEntity
import com.diavolo.gogroceriesapp.core.database.entity.GroceryListEntity
import com.diavolo.gogroceriesapp.core.database.entity.relations.ListWithItems
import kotlinx.coroutines.flow.Flow

interface GroceryListLocalDataSource {
    fun observeAllWithItems(): Flow<List<ListWithItems>>

    fun observeWithItems(id: Long): Flow<ListWithItems?>

    suspend fun insertList(list: GroceryListEntity): Long

    suspend fun updateList(list: GroceryListEntity)

    suspend fun deleteList(id: Long)

    suspend fun insertItem(item: GroceryItemEntity): Long

    suspend fun updateItem(item: GroceryItemEntity)

    suspend fun setItemChecked(id: Long, checked: Boolean)

    suspend fun deleteItem(id: Long)
}
