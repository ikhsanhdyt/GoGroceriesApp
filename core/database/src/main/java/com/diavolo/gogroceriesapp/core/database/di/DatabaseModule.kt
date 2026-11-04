package com.diavolo.gogroceriesapp.core.database.di

import android.content.Context
import androidx.room.Room
import com.diavolo.gogroceriesapp.core.database.DefaultCategoriesCallback
import com.diavolo.gogroceriesapp.core.database.GroceryDatabase
import com.diavolo.gogroceriesapp.core.database.dao.CategoryDao
import com.diavolo.gogroceriesapp.core.database.dao.GroceryItemDao
import com.diavolo.gogroceriesapp.core.database.dao.GroceryListDao
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
        )
            .addCallback(DefaultCategoriesCallback())
            .build()
    }

    @Provides
    fun provideListDao(db: GroceryDatabase): GroceryListDao = db.listDao()

    @Provides
    fun provideItemDao(db: GroceryDatabase): GroceryItemDao = db.itemDao()

    @Provides
    fun provideCategoryDao(db: GroceryDatabase): CategoryDao = db.categoryDao()
}
