package com.diavolo.gogroceriesapp.core.entity.grocery

import com.diavolo.gogroceriesapp.core.entity.common.Money

data class TripSummary(
    val actualSpent: Money,
    val estimatedPurchasedTotal: Money,
    val budget: Money?,
    val budgetRemainingRupiah: Long?,
    val purchasedItems: List<GroceryItem>,
    val skippedItems: List<GroceryItem>,
    val missingActualPriceCount: Int
)
