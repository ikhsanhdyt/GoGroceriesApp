package com.diavolo.gogroceriesapp.domain.repository

import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.GroceryList
import kotlinx.coroutines.flow.Flow

interface GroceryListRepository {
    fun getAllLists(): Flow<List<GroceryList>>
    fun getListById(id: Long): Flow<GroceryList?>
    fun getListWithItems(id: Long): Flow<Pair<GroceryList?, List<GroceryItem>>>
    suspend fun insertList(list: GroceryList): Long
    suspend fun updateList(list: GroceryList)
    suspend fun deleteList(list: GroceryList)
    suspend fun insertItem(item: GroceryItem): Long
    suspend fun updateItem(item: GroceryItem)
    suspend fun deleteItem(item: GroceryItem)
    suspend fun toggleItemChecked(itemId: Long, isChecked: Boolean)
}