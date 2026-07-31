package com.diavolo.gogroceriesapp.di

import com.diavolo.gogroceriesapp.data.repository.CategoryRepositoryImpl
import com.diavolo.gogroceriesapp.data.repository.GroceryListRepositoryImpl
import com.diavolo.gogroceriesapp.domain.repository.CategoryRepository
import com.diavolo.gogroceriesapp.domain.repository.GroceryListRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGroceryListRepository(
        impl: GroceryListRepositoryImpl
    ): GroceryListRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository
}