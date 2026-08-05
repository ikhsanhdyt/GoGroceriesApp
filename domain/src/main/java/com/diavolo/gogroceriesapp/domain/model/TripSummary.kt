package com.diavolo.gogroceriesapp.domain.model

import com.diavolo.gogroceriesapp.domain.Money

data class TripSummary(
    val actualSpent: Money,
    val estimatedPurchasedTotal: Money,
    val budget: Money?,
    val budgetRemainingRupiah: Long?,
    val purchasedItems: List<GroceryItem>,
    val skippedItems: List<GroceryItem>,
    val missingActualPriceCount: Int
)
