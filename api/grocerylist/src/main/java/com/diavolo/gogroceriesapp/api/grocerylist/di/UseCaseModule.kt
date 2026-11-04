package com.diavolo.gogroceriesapp.api.grocerylist.di

import com.diavolo.gogroceriesapp.api.grocerylist.data.repository.GroceryListRepository
import com.diavolo.gogroceriesapp.api.grocerylist.domain.calculation.ComputeActualTotalUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.calculation.ComputeActualTotalUseCaseImpl
import com.diavolo.gogroceriesapp.api.grocerylist.domain.calculation.ComputeBudgetAnalyticsUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.calculation.ComputeBudgetAnalyticsUseCaseImpl
import com.diavolo.gogroceriesapp.api.grocerylist.domain.calculation.ComputeEstimatedTotalUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.calculation.ComputeEstimatedTotalUseCaseImpl
import com.diavolo.gogroceriesapp.api.grocerylist.domain.calculation.ComputeTripSummaryUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.calculation.ComputeTripSummaryUseCaseImpl
import com.diavolo.gogroceriesapp.api.grocerylist.domain.get.GetListUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.get.GetListUseCaseImpl
import com.diavolo.gogroceriesapp.api.grocerylist.domain.get.GetListsUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.get.GetListsUseCaseImpl
import com.diavolo.gogroceriesapp.api.grocerylist.domain.item.AddItemUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.item.AddItemUseCaseImpl
import com.diavolo.gogroceriesapp.api.grocerylist.domain.item.DeleteItemUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.item.DeleteItemUseCaseImpl
import com.diavolo.gogroceriesapp.api.grocerylist.domain.item.ToggleItemCheckedUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.item.ToggleItemCheckedUseCaseImpl
import com.diavolo.gogroceriesapp.api.grocerylist.domain.item.UpdateItemUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.item.UpdateItemUseCaseImpl
import com.diavolo.gogroceriesapp.api.grocerylist.domain.list.CreateListUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.list.CreateListUseCaseImpl
import com.diavolo.gogroceriesapp.api.grocerylist.domain.list.DeleteListUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.list.DeleteListUseCaseImpl
import com.diavolo.gogroceriesapp.api.grocerylist.domain.list.UpdateListUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.list.UpdateListUseCaseImpl
import com.diavolo.gogroceriesapp.api.grocerylist.domain.shopping.FinishShoppingUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.shopping.FinishShoppingUseCaseImpl
import com.diavolo.gogroceriesapp.api.grocerylist.domain.shopping.StartShoppingUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.shopping.StartShoppingUseCaseImpl
import com.diavolo.gogroceriesapp.core.util.TimeProvider
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
    internal fun provideGetListsUseCase(repo: GroceryListRepository): GetListsUseCase =
        GetListsUseCaseImpl(repo)

    @Singleton
    @Provides
    internal fun provideGetListUseCase(repo: GroceryListRepository): GetListUseCase =
        GetListUseCaseImpl(repo)

    @Singleton
    @Provides
    internal fun provideCreateListUseCase(repo: GroceryListRepository): CreateListUseCase =
        CreateListUseCaseImpl(repo)

    @Singleton
    @Provides
    internal fun provideUpdateListUseCase(repo: GroceryListRepository): UpdateListUseCase =
        UpdateListUseCaseImpl(repo)

    @Singleton
    @Provides
    internal fun provideDeleteListUseCase(repo: GroceryListRepository): DeleteListUseCase =
        DeleteListUseCaseImpl(repo)

    @Singleton
    @Provides
    internal fun provideAddItemUseCase(repo: GroceryListRepository): AddItemUseCase =
        AddItemUseCaseImpl(repo)

    @Singleton
    @Provides
    internal fun provideUpdateItemUseCase(repo: GroceryListRepository): UpdateItemUseCase =
        UpdateItemUseCaseImpl(repo)

    @Singleton
    @Provides
    internal fun provideDeleteItemUseCase(repo: GroceryListRepository): DeleteItemUseCase =
        DeleteItemUseCaseImpl(repo)

    @Singleton
    @Provides
    internal fun provideToggleItemCheckedUseCase(
        repo: GroceryListRepository
    ): ToggleItemCheckedUseCase = ToggleItemCheckedUseCaseImpl(repo)

    @Singleton
    @Provides
    internal fun provideStartShoppingUseCase(repo: GroceryListRepository): StartShoppingUseCase =
        StartShoppingUseCaseImpl(repo)

    @Singleton
    @Provides
    internal fun provideFinishShoppingUseCase(
        repo: GroceryListRepository,
        timeProvider: TimeProvider
    ): FinishShoppingUseCase = FinishShoppingUseCaseImpl(repo, timeProvider)

    @Singleton
    @Provides
    internal fun provideComputeActualTotalUseCase(): ComputeActualTotalUseCase =
        ComputeActualTotalUseCaseImpl()

    @Singleton
    @Provides
    internal fun provideComputeEstimatedTotalUseCase(): ComputeEstimatedTotalUseCase =
        ComputeEstimatedTotalUseCaseImpl()

    @Singleton
    @Provides
    internal fun provideComputeTripSummaryUseCase(
        computeActualTotal: ComputeActualTotalUseCase,
        computeEstimatedTotal: ComputeEstimatedTotalUseCase
    ): ComputeTripSummaryUseCase =
        ComputeTripSummaryUseCaseImpl(computeActualTotal, computeEstimatedTotal)

    @Singleton
    @Provides
    internal fun provideComputeBudgetAnalyticsUseCase(
        computeActualTotal: ComputeActualTotalUseCase,
        computeEstimatedTotal: ComputeEstimatedTotalUseCase
    ): ComputeBudgetAnalyticsUseCase =
        ComputeBudgetAnalyticsUseCaseImpl(computeActualTotal, computeEstimatedTotal)
}
