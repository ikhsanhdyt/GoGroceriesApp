package com.diavolo.gogroceriesapp.api.grocerylist.di

import com.diavolo.gogroceriesapp.api.grocerylist.data.local.service.GroceryListLocalDataSource
import com.diavolo.gogroceriesapp.api.grocerylist.data.repository.GroceryListRepository
import com.diavolo.gogroceriesapp.api.grocerylist.data.repository.GroceryListRepositoryImpl
import com.diavolo.gogroceriesapp.core.util.TimeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class GroceryListModule {

    @Singleton
    @Provides
    internal fun provideGroceryListRepository(
        local: GroceryListLocalDataSource,
        timeProvider: TimeProvider
    ): GroceryListRepository = GroceryListRepositoryImpl(local, timeProvider)
}
