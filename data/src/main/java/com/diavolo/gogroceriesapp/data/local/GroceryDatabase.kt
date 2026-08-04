package com.diavolo.gogroceriesapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.diavolo.gogroceriesapp.data.local.dao.CategoryDao
import com.diavolo.gogroceriesapp.data.local.dao.GroceryItemDao
import com.diavolo.gogroceriesapp.data.local.dao.GroceryListDao
import com.diavolo.gogroceriesapp.data.local.entity.CategoryEntity
import com.diavolo.gogroceriesapp.data.local.entity.GroceryItemEntity
import com.diavolo.gogroceriesapp.data.local.entity.GroceryListEntity

@Database(
    entities = [
        GroceryListEntity::class,
        GroceryItemEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class GroceryDatabase : RoomDatabase() {
    abstract fun listDao(): GroceryListDao
    abstract fun itemDao(): GroceryItemDao
    abstract fun categoryDao(): CategoryDao
}
