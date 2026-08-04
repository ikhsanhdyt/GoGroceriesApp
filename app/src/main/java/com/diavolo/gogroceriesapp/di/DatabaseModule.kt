package com.diavolo.gogroceriesapp.di

import android.content.Context
import androidx.room.Room
import com.diavolo.gogroceriesapp.data.local.GroceryDatabase
import com.diavolo.gogroceriesapp.data.local.dao.CategoryDao
import com.diavolo.gogroceriesapp.data.local.dao.GroceryItemDao
import com.diavolo.gogroceriesapp.data.local.dao.GroceryListDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): GroceryDatabase {
        return Room.databaseBuilder(
            ctx,
            GroceryDatabase::class.java,
            "groceries.db"
        ).build()
    }

    @Provides
    fun provideListDao(db: GroceryDatabase): GroceryListDao = db.listDao()

    @Provides
    fun provideItemDao(db: GroceryDatabase): GroceryItemDao = db.itemDao()

    @Provides
    fun provideCategoryDao(db: GroceryDatabase): CategoryDao = db.categoryDao()
}
