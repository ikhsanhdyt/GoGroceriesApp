package com.diavolo.gogroceriesapp.domain.model

import com.diavolo.gogroceriesapp.domain.Money

data class BudgetAnalytics(
    val totalSpent: Money,
    val averageSpentPerTrip: Money,
    val estimatedPurchasedTotal: Money,
    val estimateVarianceRupiah: Long,
    val completedTripCount: Int,
    val fullyPricedBudgetTripCount: Int,
    val onBudgetTripCount: Int,
    val budgetAdherencePercent: Int,
    val missingActualPriceCount: Int,
    val spendingByCategory: List<CategorySpending>,
    val recentTrips: List<TripSpending>
)

data class CategorySpending(
    val categoryId: Long?,
    val categoryName: String,
    val colorHex: String,
    val amount: Money,
    val sharePercent: Float
)

data class TripSpending(
    val listId: Long,
    val listName: String,
    val completedAt: Long,
    val amount: Money
)
