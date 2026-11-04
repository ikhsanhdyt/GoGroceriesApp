package com.diavolo.gogroceriesapp.api.grocerylist.domain.calculation

import com.diavolo.gogroceriesapp.core.entity.common.Money
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryItem
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryList
import com.diavolo.gogroceriesapp.core.entity.grocery.ListStatus
import com.diavolo.gogroceriesapp.core.entity.grocery.TripSummary
import javax.inject.Inject

class ComputeTripSummaryUseCaseImpl @Inject constructor(
    private val computeActualTotalUseCase: ComputeActualTotalUseCase,
    private val computeEstimatedTotalUseCase: ComputeEstimatedTotalUseCase
) : ComputeTripSummaryUseCase {

    override fun invoke(list: GroceryList): TripSummary {
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
