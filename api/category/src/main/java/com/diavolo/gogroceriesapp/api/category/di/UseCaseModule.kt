package com.diavolo.gogroceriesapp.api.category.di

import com.diavolo.gogroceriesapp.api.category.data.repository.CategoryRepository
import com.diavolo.gogroceriesapp.api.category.domain.add.AddCategoryUseCase
import com.diavolo.gogroceriesapp.api.category.domain.add.AddCategoryUseCaseImpl
import com.diavolo.gogroceriesapp.api.category.domain.delete.DeleteCategoryUseCase
import com.diavolo.gogroceriesapp.api.category.domain.delete.DeleteCategoryUseCaseImpl
import com.diavolo.gogroceriesapp.api.category.domain.get.GetCategoriesUseCase
import com.diavolo.gogroceriesapp.api.category.domain.get.GetCategoriesUseCaseImpl
import com.diavolo.gogroceriesapp.api.category.domain.update.UpdateCategoryUseCase
import com.diavolo.gogroceriesapp.api.category.domain.update.UpdateCategoryUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {

    @Singleton
    @Provides
    internal fun provideGetCategoriesUseCase(repo: CategoryRepository): GetCategoriesUseCase =
        GetCategoriesUseCaseImpl(repo)

    @Singleton
    @Provides
    internal fun provideAddCategoryUseCase(repo: CategoryRepository): AddCategoryUseCase =
        AddCategoryUseCaseImpl(repo)

    @Singleton
    @Provides
    internal fun provideUpdateCategoryUseCase(repo: CategoryRepository): UpdateCategoryUseCase =
        UpdateCategoryUseCaseImpl(repo)

    @Singleton
    @Provides
    internal fun provideDeleteCategoryUseCase(repo: CategoryRepository): DeleteCategoryUseCase =
        DeleteCategoryUseCaseImpl(repo)
}
