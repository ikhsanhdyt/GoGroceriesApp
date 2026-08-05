package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.Money
import com.diavolo.gogroceriesapp.domain.model.BudgetAnalytics
import com.diavolo.gogroceriesapp.domain.model.Category
import com.diavolo.gogroceriesapp.domain.model.CategorySpending
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.model.ListStatus
import com.diavolo.gogroceriesapp.domain.model.TripSpending
import kotlin.math.roundToInt
import javax.inject.Inject

class ComputeBudgetAnalyticsUseCase @Inject constructor(
    private val computeActualTotalUseCase: ComputeActualTotalUseCase,
    private val computeEstimatedTotalUseCase: ComputeEstimatedTotalUseCase
) {
    operator fun invoke(
        lists: List<GroceryList>,
        categories: List<Category>
    ): BudgetAnalytics {
        val completedTrips = lists
            .filter { it.status == ListStatus.Completed }
            .sortedByDescending(GroceryList::updatedAt)
        val purchasedItems = completedTrips.flatMap { list ->
            list.items.filter(GroceryItem::isChecked)
        }
        val totalSpent = computeActualTotalUseCase(purchasedItems)
        val estimatedTotal = computeEstimatedTotalUseCase(purchasedItems)
        val fullyPricedBudgetTrips = completedTrips.filter { list ->
            val purchased = list.items.filter(GroceryItem::isChecked)
            list.budgetRupiah != null &&
                purchased.isNotEmpty() &&
                purchased.all { it.actualPriceRupiah != null }
        }
        val onBudgetTrips = fullyPricedBudgetTrips.count { list ->
            computeActualTotalUseCase(list.items.filter(GroceryItem::isChecked)).rupiah <=
                requireNotNull(list.budgetRupiah)
        }
        val categoryById = categories.associateBy(Category::id)

        return BudgetAnalytics(
            totalSpent = totalSpent,
            averageSpentPerTrip = Money(
                if (completedTrips.isEmpty()) 0L
                else totalSpent.rupiah / completedTrips.size
            ),
            estimatedPurchasedTotal = estimatedTotal,
            estimateVarianceRupiah = estimatedTotal.rupiah - totalSpent.rupiah,
            completedTripCount = completedTrips.size,
            fullyPricedBudgetTripCount = fullyPricedBudgetTrips.size,
            onBudgetTripCount = onBudgetTrips,
            budgetAdherencePercent = if (fullyPricedBudgetTrips.isEmpty()) {
                0
            } else {
                (onBudgetTrips * 100f / fullyPricedBudgetTrips.size).roundToInt()
            },
            missingActualPriceCount = purchasedItems.count {
                it.actualPriceRupiah == null
            },
            spendingByCategory = computeCategorySpending(
                items = purchasedItems,
                categoryById = categoryById,
                totalSpent = totalSpent
            ),
            recentTrips = completedTrips.take(6).map { list ->
                TripSpending(
                    listId = list.id,
                    listName = list.name,
                    completedAt = list.updatedAt,
                    amount = computeActualTotalUseCase(
                        list.items.filter(GroceryItem::isChecked)
                    )
                )
            }.reversed()
        )
    }

    private fun computeCategorySpending(
        items: List<GroceryItem>,
        categoryById: Map<Long, Category>,
        totalSpent: Money
    ): List<CategorySpending> = items
        .filter { it.actualPriceRupiah != null }
        .groupBy(GroceryItem::categoryId)
        .map { (categoryId, categoryItems) ->
            val amount = computeActualTotalUseCase(categoryItems)
            val category = categoryId?.let(categoryById::get)
            CategorySpending(
                categoryId = categoryId,
                categoryName = category?.name ?: "Other",
                colorHex = category?.colorHex ?: "#7A7A7A",
                amount = amount,
                sharePercent = if (totalSpent.rupiah == 0L) {
                    0f
                } else {
                    amount.rupiah.toFloat() / totalSpent.rupiah
                }
            )
        }
        .sortedByDescending { it.amount.rupiah }
}
