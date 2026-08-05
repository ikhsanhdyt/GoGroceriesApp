package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.Money
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.model.ListStatus
import com.diavolo.gogroceriesapp.domain.model.UnitOfMeasure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ComputeTripSummaryUseCaseTest {

    private val useCase = ComputeTripSummaryUseCase(
        computeActualTotalUseCase = ComputeActualTotalUseCase(),
        computeEstimatedTotalUseCase = ComputeEstimatedTotalUseCase()
    )

    @Test
    fun `summary separates purchased items and calculates budget remaining`() {
        val list = completedList(
            items = listOf(
                item(id = 1, checked = true, estimatedPrice = 12_000, actualPrice = 10_000),
                item(id = 2, checked = true, estimatedPrice = 20_000, actualPrice = null),
                item(id = 3, checked = false, estimatedPrice = 50_000, actualPrice = null)
            )
        )

        val summary = useCase(list)

        assertEquals(Money(10_000), summary.actualSpent)
        assertEquals(Money(32_000), summary.estimatedPurchasedTotal)
        assertEquals(90_000L, summary.budgetRemainingRupiah)
        assertEquals(listOf(1L, 2L), summary.purchasedItems.map { it.id })
        assertEquals(listOf(3L), summary.skippedItems.map { it.id })
        assertEquals(1, summary.missingActualPriceCount)
    }

    @Test
    fun `summary rejects an active list`() {
        assertThrows(IllegalArgumentException::class.java) {
            useCase(completedList(emptyList()).copy(status = ListStatus.Active))
        }
    }

    private fun completedList(items: List<GroceryItem>) = GroceryList(
        id = 1,
        name = "Weekly groceries",
        status = ListStatus.Completed,
        budgetRupiah = 100_000,
        createdAt = 1,
        updatedAt = 2,
        items = items
    )

    private fun item(
        id: Long,
        checked: Boolean,
        estimatedPrice: Long?,
        actualPrice: Long?
    ) = GroceryItem(
        id = id,
        listId = 1,
        categoryId = null,
        name = "Item $id",
        quantity = 1.0,
        unit = UnitOfMeasure.PIECE,
        estimatedPriceRupiah = estimatedPrice,
        actualPriceRupiah = actualPrice,
        isChecked = checked,
        notes = null,
        position = id.toInt()
    )
}
