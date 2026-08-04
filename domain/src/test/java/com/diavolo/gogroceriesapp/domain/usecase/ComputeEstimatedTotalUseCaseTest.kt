package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.Money
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.UnitOfMeasure
import org.junit.Assert.assertEquals
import org.junit.Test

class ComputeEstimatedTotalUseCaseTest {

    private val useCase = ComputeEstimatedTotalUseCase()

    @Test
    fun `total uses whole rupiah and item quantity`() {
        val items = listOf(
            groceryItem(quantity = 2.0, estimatedPriceRupiah = 12_500),
            groceryItem(quantity = 1.5, estimatedPriceRupiah = 10_000)
        )

        assertEquals(Money(40_000), useCase(items))
    }

    @Test
    fun `items without an estimated price contribute zero`() {
        val items = listOf(
            groceryItem(quantity = 4.0, estimatedPriceRupiah = null)
        )

        assertEquals(Money.zero(), useCase(items))
    }

    @Test
    fun `money formats as Indonesian rupiah`() {
        assertEquals("Rp 1.250.000", Money(1_250_000).toString())
    }

    private fun groceryItem(
        quantity: Double,
        estimatedPriceRupiah: Long?
    ) = GroceryItem(
        listId = 1,
        categoryId = null,
        name = "Test item",
        quantity = quantity,
        unit = UnitOfMeasure.PIECE,
        estimatedPriceRupiah = estimatedPriceRupiah,
        actualPriceRupiah = null,
        isChecked = false,
        notes = null,
        position = 0
    )
}
