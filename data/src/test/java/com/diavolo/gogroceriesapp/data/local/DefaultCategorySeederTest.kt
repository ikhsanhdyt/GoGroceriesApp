package com.diavolo.gogroceriesapp.data.local

import com.diavolo.gogroceriesapp.data.local.dao.CategoryDao
import com.diavolo.gogroceriesapp.data.local.entity.CategoryEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DefaultCategorySeederTest {

    private val categoryDao = mockk<CategoryDao>()
    private val seeder = DefaultCategorySeeder(categoryDao)

    @Test
    fun `seed inserts only missing categories`() = runBlocking {
        val insertedCategories = slot<List<CategoryEntity>>()
        coEvery { categoryDao.getNames() } returns listOf("produce")
        coEvery { categoryDao.insertAll(capture(insertedCategories)) } returns Unit

        seeder.seed()

        assertEquals(DEFAULT_CATEGORIES.size - 1, insertedCategories.captured.size)
        assertFalse(insertedCategories.captured.any { it.name == "Produce" })
        coVerify(exactly = 1) { categoryDao.insertAll(any()) }
    }

    @Test
    fun `seed does nothing when every default exists`() = runBlocking {
        coEvery { categoryDao.getNames() } returns DEFAULT_CATEGORIES.map { it.name }

        seeder.seed()

        coVerify(exactly = 0) { categoryDao.insertAll(any()) }
    }
}
