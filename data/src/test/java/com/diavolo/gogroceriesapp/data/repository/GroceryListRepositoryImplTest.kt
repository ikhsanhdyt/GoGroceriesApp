package com.diavolo.gogroceriesapp.data.repository

import com.diavolo.gogroceriesapp.data.local.dao.GroceryItemDao
import com.diavolo.gogroceriesapp.data.local.dao.GroceryListDao
import com.diavolo.gogroceriesapp.data.local.entity.GroceryItemEntity
import com.diavolo.gogroceriesapp.data.local.entity.GroceryListEntity
import com.diavolo.gogroceriesapp.data.local.entity.relations.ListWithItems
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.UnitOfMeasure
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GroceryListRepositoryImplTest {

    private val listDao = mockk<GroceryListDao>()
    private val itemDao = mockk<GroceryItemDao>()
    private val repository = GroceryListRepositoryImpl(listDao, itemDao)

    @Test
    fun `observeLists includes items ordered by position`() = runBlocking {
        every { listDao.observeAllWithItems() } returns flowOf(
            listOf(
                ListWithItems(
                    list = GroceryListEntity(
                        id = 1,
                        name = "Weekly groceries",
                        status = "Draft",
                        budgetRupiah = 250_000,
                        createdAt = 1,
                        updatedAt = 2
                    ),
                    items = listOf(
                        groceryItem(id = 2, name = "Second", position = 2),
                        groceryItem(id = 1, name = "First", position = 1)
                    )
                )
            )
        )

        val list = repository.observeLists().first().single()

        assertEquals(250_000L, list.budgetRupiah)
        assertEquals(listOf("First", "Second"), list.items.map { it.name })
    }

    @Test
    fun `toggleChecked updates the requested item`() = runBlocking {
        coEvery { itemDao.setChecked(id = 42, checked = true) } returns Unit

        repository.toggleChecked(itemId = 42, checked = true)

        coVerify(exactly = 1) { itemDao.setChecked(id = 42, checked = true) }
    }

    @Test
    fun `updateItem persists the mapped item`() = runBlocking {
        val item = domainItem(id = 42, name = "Updated bananas")
        coEvery { itemDao.update(any()) } returns Unit

        repository.updateItem(item)

        coVerify(exactly = 1) {
            itemDao.update(match { it.id == 42L && it.name == "Updated bananas" })
        }
    }

    @Test
    fun `deleteItem removes the requested item`() = runBlocking {
        coEvery { itemDao.delete(id = 42) } returns Unit

        repository.deleteItem(itemId = 42)

        coVerify(exactly = 1) { itemDao.delete(id = 42) }
    }

    private fun groceryItem(
        id: Long,
        name: String,
        position: Int
    ) = GroceryItemEntity(
        id = id,
        listId = 1,
        categoryId = null,
        name = name,
        quantity = 1.0,
        unit = UnitOfMeasure.PIECE.name,
        estimatedPriceRupiah = 10_000,
        actualPriceRupiah = null,
        isChecked = false,
        notes = null,
        position = position
    )

    private fun domainItem(
        id: Long,
        name: String
    ) = GroceryItem(
        id = id,
        listId = 1,
        categoryId = null,
        name = name,
        quantity = 1.0,
        unit = UnitOfMeasure.PIECE,
        estimatedPriceRupiah = 10_000,
        actualPriceRupiah = null,
        isChecked = false,
        notes = null,
        position = 0
    )
}
