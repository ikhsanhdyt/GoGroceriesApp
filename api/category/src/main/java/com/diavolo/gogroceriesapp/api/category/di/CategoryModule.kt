package com.diavolo.gogroceriesapp.api.category.di

import com.diavolo.gogroceriesapp.api.category.data.local.service.CategoryLocalDataSource
import com.diavolo.gogroceriesapp.api.category.data.repository.CategoryRepository
import com.diavolo.gogroceriesapp.api.category.data.repository.CategoryRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CategoryModule {

    @Singleton
    @Provides
    internal fun provideCategoryRepository(local: CategoryLocalDataSource): CategoryRepository =
        CategoryRepositoryImpl(local)
}
