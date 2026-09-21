package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.Money
import com.diavolo.gogroceriesapp.domain.model.Category
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.model.ListStatus
import com.diavolo.gogroceriesapp.domain.model.UnitOfMeasure
import org.junit.Assert.assertEquals
import org.junit.Test

class ComputeBudgetAnalyticsUseCaseTest {

    private val useCase = ComputeBudgetAnalyticsUseCase(
        computeActualTotalUseCase = ComputeActualTotalUseCase(),
        computeEstimatedTotalUseCase = ComputeEstimatedTotalUseCase()
    )

    @Test
    fun `analytics aggregates completed trips and excludes incomplete price coverage from adherence`() {
        val lists = listOf(
            list(
                id = 1,
                status = ListStatus.Completed,
                updatedAt = 100,
                budget = 50_000,
                items = listOf(item(1, categoryId = 10, estimated = 30_000, actual = 25_000))
            ),
            list(
                id = 2,
                status = ListStatus.Completed,
                updatedAt = 200,
                budget = 10_000,
                items = listOf(item(2, categoryId = null, estimated = 12_000, actual = null))
            ),
            list(
                id = 3,
                status = ListStatus.Active,
                updatedAt = 300,
                budget = 100_000,
                items = listOf(item(3, categoryId = 10, estimated = 80_000, actual = 90_000))
            )
        )

        val analytics = useCase(
            lists = lists,
            categories = listOf(Category(10, "Produce", "#4F8A5B", 0))
        )

        assertEquals(Money(25_000), analytics.totalSpent)
        assertEquals(Money(12_500), analytics.averageSpentPerTrip)
        assertEquals(Money(42_000), analytics.estimatedPurchasedTotal)
        assertEquals(2, analytics.completedTripCount)
        assertEquals(1, analytics.fullyPricedBudgetTripCount)
        assertEquals(1, analytics.onBudgetTripCount)
        assertEquals(100, analytics.budgetAdherencePercent)
        assertEquals(1, analytics.missingActualPriceCount)
        assertEquals("Produce", analytics.spendingByCategory.single().categoryName)
        assertEquals(listOf(1L, 2L), analytics.recentTrips.map { it.listId })
    }

    @Test
    fun `recent trips follow completion time, not last edit time`() {
        val lists = listOf(
            // Finished first, edited most recently.
            list(1, ListStatus.Completed, updatedAt = 900, budget = null, completedAt = 100),
            list(2, ListStatus.Completed, updatedAt = 500, budget = null, completedAt = 200)
        )

        val analytics = useCase(lists, emptyList())

        assertEquals(listOf(1L, 2L), analytics.recentTrips.map { it.listId })
        assertEquals(listOf(100L, 200L), analytics.recentTrips.map { it.completedAt })
    }

    @Test
    fun `completed trip without completion time falls back to its update time`() {
        val lists = listOf(
            list(1, ListStatus.Completed, updatedAt = 300, budget = null, completedAt = null)
        )

        val analytics = useCase(lists, emptyList())

        assertEquals(300L, analytics.recentTrips.single().completedAt)
    }

    @Test
    fun `analytics returns zero values without completed trips`() {
        val analytics = useCase(emptyList(), emptyList())

        assertEquals(Money.zero(), analytics.totalSpent)
        assertEquals(0, analytics.completedTripCount)
        assertEquals(0, analytics.budgetAdherencePercent)
        assertEquals(emptyList<Any>(), analytics.spendingByCategory)
    }

    private fun list(
        id: Long,
        status: ListStatus,
        updatedAt: Long,
        budget: Long?,
        items: List<GroceryItem> = emptyList(),
        completedAt: Long? = null
    ) = GroceryList(
        id = id,
        name = "Trip $id",
        status = status,
        budgetRupiah = budget,
        createdAt = updatedAt - 1,
        updatedAt = updatedAt,
        items = items,
        completedAt = completedAt
    )

    private fun item(
        id: Long,
        categoryId: Long?,
        estimated: Long?,
        actual: Long?
    ) = GroceryItem(
        id = id,
        listId = id,
        categoryId = categoryId,
        name = "Item $id",
        quantity = 1.0,
        unit = UnitOfMeasure.PIECE,
        estimatedPriceRupiah = estimated,
        actualPriceRupiah = actual,
        isChecked = true,
        notes = null,
        position = 0
    )
}
