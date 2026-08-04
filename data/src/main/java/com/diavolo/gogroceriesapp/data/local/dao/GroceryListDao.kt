package com.diavolo.gogroceriesapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.diavolo.gogroceriesapp.data.local.entity.GroceryListEntity
import com.diavolo.gogroceriesapp.data.local.entity.relations.ListWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryListDao {
    @Transaction
    @Query("SELECT * FROM grocery_lists ORDER BY updatedAt DESC")
    fun observeAllWithItems(): Flow<List<ListWithItems>>

    @Transaction
    @Query("SELECT * FROM grocery_lists WHERE id = :id")
    fun observeWithItems(id: Long): Flow<ListWithItems?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: GroceryListEntity): Long

    @Update
    suspend fun update(list: GroceryListEntity)

    @Query("DELETE FROM grocery_lists WHERE id = :id")
    suspend fun delete(id: Long)
}
