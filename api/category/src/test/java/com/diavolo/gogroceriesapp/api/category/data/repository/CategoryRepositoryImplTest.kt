package com.diavolo.gogroceriesapp.api.category.data.repository

import com.diavolo.gogroceriesapp.api.category.data.local.service.CategoryLocalDataSourceImpl
import com.diavolo.gogroceriesapp.core.database.dao.CategoryDao
import com.diavolo.gogroceriesapp.core.database.entity.CategoryEntity
import com.diavolo.gogroceriesapp.core.entity.category.Category
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class CategoryRepositoryImplTest {

    private val categoryDao = mockk<CategoryDao>()
    private val repository = CategoryRepositoryImpl(CategoryLocalDataSourceImpl(categoryDao))

    @Test
    fun `update goes through the dao update instead of a replacing insert`() = runBlocking {
        coEvery { categoryDao.update(any()) } returns Unit

        repository.updateCategory(
            Category(id = 3, name = "Roti", colorHex = "#C98B4A", aisleOrder = 3)
        )

        coVerify(exactly = 1) {
            categoryDao.update(
                CategoryEntity(id = 3, name = "Roti", colorHex = "#C98B4A", aisleOrder = 3)
            )
        }
        coVerify(exactly = 0) { categoryDao.insert(any()) }
    }

    @Test
    fun `delete removes the category by id`() = runBlocking {
        coEvery { categoryDao.delete(any()) } returns Unit

        repository.deleteCategory(3)

        coVerify(exactly = 1) { categoryDao.delete(3) }
    }
}
