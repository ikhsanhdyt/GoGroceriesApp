package com.diavolo.gogroceriesapp.api.grocerylist.domain.calculation

import com.diavolo.gogroceriesapp.core.entity.common.Money
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryItem
import com.diavolo.gogroceriesapp.core.entity.grocery.UnitOfMeasure
import org.junit.Assert.assertEquals
import org.junit.Test

class ComputeEstimatedTotalUseCaseTest {

    private val useCase = ComputeEstimatedTotalUseCaseImpl()

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

    @Test
    fun `actual total uses actual unit prices and quantity`() {
        val items = listOf(
            groceryItem(
                quantity = 2.0,
                estimatedPriceRupiah = 10_000,
                actualPriceRupiah = 12_500
            ),
            groceryItem(
                quantity = 1.5,
                estimatedPriceRupiah = 20_000,
                actualPriceRupiah = 8_000
            )
        )

        assertEquals(Money(37_000), ComputeActualTotalUseCaseImpl()(items))
    }

    private fun groceryItem(
        quantity: Double,
        estimatedPriceRupiah: Long?,
        actualPriceRupiah: Long? = null
    ) = GroceryItem(
        listId = 1,
        categoryId = null,
        name = "Test item",
        quantity = quantity,
        unit = UnitOfMeasure.PIECE,
        estimatedPriceRupiah = estimatedPriceRupiah,
        actualPriceRupiah = actualPriceRupiah,
        isChecked = false,
        notes = null,
        position = 0
    )
}
