package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.Money
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.model.ListStatus
import com.diavolo.gogroceriesapp.domain.model.TripSummary
import javax.inject.Inject

class ComputeTripSummaryUseCase @Inject constructor(
    private val computeActualTotalUseCase: ComputeActualTotalUseCase,
    private val computeEstimatedTotalUseCase: ComputeEstimatedTotalUseCase
) {
    operator fun invoke(list: GroceryList): TripSummary {
        require(list.status == ListStatus.Completed) {
            "Trip summaries are available only for completed lists."
        }

        val purchasedItems = list.items.filter(GroceryItem::isChecked)
        val skippedItems = list.items.filterNot(GroceryItem::isChecked)
        val actualSpent = computeActualTotalUseCase(purchasedItems)
        val budget = list.budgetRupiah?.let(::Money)

        return TripSummary(
            actualSpent = actualSpent,
            estimatedPurchasedTotal = computeEstimatedTotalUseCase(purchasedItems),
            budget = budget,
            budgetRemainingRupiah = budget?.rupiah?.minus(actualSpent.rupiah),
            purchasedItems = purchasedItems,
            skippedItems = skippedItems,
            missingActualPriceCount = purchasedItems.count {
                it.actualPriceRupiah == null
            }
        )
    }
}
