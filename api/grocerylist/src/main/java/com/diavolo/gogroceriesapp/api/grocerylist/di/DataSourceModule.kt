package com.diavolo.gogroceriesapp.api.grocerylist.di

import com.diavolo.gogroceriesapp.api.grocerylist.data.local.service.GroceryListLocalDataSource
import com.diavolo.gogroceriesapp.api.grocerylist.data.local.service.GroceryListLocalDataSourceImpl
import com.diavolo.gogroceriesapp.core.database.dao.GroceryItemDao
import com.diavolo.gogroceriesapp.core.database.dao.GroceryListDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataSourceModule {

    @Singleton
    @Provides
    internal fun provideGroceryListLocalDataSource(
        listDao: GroceryListDao,
        itemDao: GroceryItemDao
    ): GroceryListLocalDataSource = GroceryListLocalDataSourceImpl(listDao, itemDao)
}
