package com.diavolo.gogroceriesapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.diavolo.gogroceriesapp.core.database.dao.CategoryDao
import com.diavolo.gogroceriesapp.core.database.dao.GroceryItemDao
import com.diavolo.gogroceriesapp.core.database.dao.GroceryListDao
import com.diavolo.gogroceriesapp.core.database.entity.CategoryEntity
import com.diavolo.gogroceriesapp.core.database.entity.GroceryItemEntity
import com.diavolo.gogroceriesapp.core.database.entity.GroceryListEntity

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
