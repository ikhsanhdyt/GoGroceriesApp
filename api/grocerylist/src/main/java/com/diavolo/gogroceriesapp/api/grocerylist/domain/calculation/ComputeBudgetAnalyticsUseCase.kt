package com.diavolo.gogroceriesapp.api.grocerylist.domain.calculation

import com.diavolo.gogroceriesapp.core.entity.analytics.BudgetAnalytics
import com.diavolo.gogroceriesapp.core.entity.category.Category
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryList

interface ComputeBudgetAnalyticsUseCase {
    operator fun invoke(
        lists: List<GroceryList>,
        categories: List<Category>
    ): BudgetAnalytics
}
