package com.diavolo.gogroceriesapp.api.grocerylist.data.repository

import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryItem
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryList
import kotlinx.coroutines.flow.Flow

interface GroceryListRepository {
    fun observeLists(): Flow<List<GroceryList>>

    fun observeListWithItems(id: Long): Flow<GroceryList?>

    suspend fun create(name: String, budgetRupiah: Long?): Long

    suspend fun update(list: GroceryList)

    suspend fun delete(id: Long)

    suspend fun addItem(item: GroceryItem): Long

    suspend fun updateItem(item: GroceryItem)

    suspend fun toggleChecked(itemId: Long, checked: Boolean)

    suspend fun deleteItem(itemId: Long)
}
