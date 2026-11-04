package com.diavolo.gogroceriesapp.api.category.di

import com.diavolo.gogroceriesapp.api.category.data.local.service.CategoryLocalDataSource
import com.diavolo.gogroceriesapp.api.category.data.local.service.CategoryLocalDataSourceImpl
import com.diavolo.gogroceriesapp.core.database.dao.CategoryDao
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
    internal fun provideCategoryLocalDataSource(dao: CategoryDao): CategoryLocalDataSource =
        CategoryLocalDataSourceImpl(dao)
}
