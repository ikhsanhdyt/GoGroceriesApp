package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.model.ListStatus
import com.diavolo.gogroceriesapp.domain.model.UnitOfMeasure
import com.diavolo.gogroceriesapp.domain.repository.GroceryListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ShoppingStatusUseCaseTest {

    private val repository = RecordingRepository()

    @Test
    fun `start shopping changes a draft list to active`() = runBlocking {
        StartShoppingUseCase(repository)(shoppingList(ListStatus.Draft))

        assertEquals(ListStatus.Active, repository.updatedList?.status)
    }

    @Test
    fun `finish shopping changes an active list to completed`() = runBlocking {
        FinishShoppingUseCase(repository)(shoppingList(ListStatus.Active))

        assertEquals(ListStatus.Completed, repository.updatedList?.status)
    }

    @Test
    fun `an empty list cannot start shopping`() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                StartShoppingUseCase(repository)(
                    shoppingList(ListStatus.Draft).copy(items = emptyList())
                )
            }
        }
    }

    private fun shoppingList(status: ListStatus) = GroceryList(
        id = 1,
        name = "Weekly groceries",
        status = status,
        budgetRupiah = 250_000,
        createdAt = 1,
        updatedAt = 1,
        items = listOf(
            GroceryItem(
                id = 1,
                listId = 1,
                categoryId = null,
                name = "Bananas",
                quantity = 1.0,
                unit = UnitOfMeasure.PACK,
                estimatedPriceRupiah = 25_000,
                actualPriceRupiah = null,
                isChecked = false,
                notes = null,
                position = 0
            )
        )
    )

    private class RecordingRepository : GroceryListRepository {
        var updatedList: GroceryList? = null

        override fun observeLists(): Flow<List<GroceryList>> = emptyFlow()

        override fun observeListWithItems(id: Long): Flow<GroceryList?> = emptyFlow()

        override suspend fun create(name: String, budgetRupiah: Long?): Long = 1

        override suspend fun update(list: GroceryList) {
            updatedList = list
        }

        override suspend fun delete(id: Long) = Unit

        override suspend fun addItem(item: GroceryItem): Long = 1

        override suspend fun updateItem(item: GroceryItem) = Unit

        override suspend fun toggleChecked(itemId: Long, checked: Boolean) = Unit

        override suspend fun deleteItem(itemId: Long) = Unit
    }
}
