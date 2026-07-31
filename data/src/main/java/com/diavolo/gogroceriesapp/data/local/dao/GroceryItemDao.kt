package com.diavolo.gogroceriesapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.diavolo.gogroceriesapp.data.local.entity.GroceryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryItemDao {
    @Query("SELECT * FROM grocery_items WHERE listId = :listId ORDER BY position ASC")
    fun observeByList(listId: Long): Flow<List<GroceryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: GroceryItemEntity): Long

    @Update
    suspend fun update(item: GroceryItemEntity)

    @Query("UPDATE grocery_items SET isChecked = :checked WHERE id = :id")
    suspend fun setChecked(id: Long, checked: Boolean)

    @Query("DELETE FROM grocery_items WHERE id = :id")
    suspend fun delete(id: Long)
}