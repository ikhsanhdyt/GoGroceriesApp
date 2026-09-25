package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.FakeCategoryRepository
import com.diavolo.gogroceriesapp.domain.model.Category
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryUseCasesTest {

    private val repository = FakeCategoryRepository(
        listOf(
            Category(id = 1, name = "Produce", colorHex = "#4F8A5B", aisleOrder = 0),
            Category(id = 2, name = "Bakery", colorHex = "#C98B4A", aisleOrder = 3)
        )
    )
    private val addCategory = AddCategoryUseCase(repository)
    private val updateCategory = UpdateCategoryUseCase(repository)
    private val deleteCategory = DeleteCategoryUseCase(repository)

    @Test
    fun `add trims the name and places the category after the existing ones`() = runBlocking {
        val id = addCategory("  Drinks ", "#123456")

        val added = repository.categories.value.single { it.id == id }
        assertEquals("Drinks", added.name)
        assertEquals("#123456", added.colorHex)
        assertEquals(4, added.aisleOrder)
    }

    @Test
    fun `add into an empty repository starts at aisle order zero`() = runBlocking {
        repository.categories.value = emptyList()

        val id = addCategory("Drinks", "#123456")

        assertEquals(0, repository.categories.value.single { it.id == id }.aisleOrder)
    }

    @Test
    fun `add rejects a blank name`() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { addCategory("   ", "#123456") }
        }
        assertEquals(2, repository.categories.value.size)
    }

    @Test
    fun `add rejects a name that differs only in case and spacing`() {
        assertThrows(DuplicateCategoryNameException::class.java) {
            runBlocking { addCategory(" produce ", "#123456") }
        }
        assertEquals(2, repository.categories.value.size)
    }

    @Test
    fun `update allows keeping the same name with a new color`() = runBlocking {
        updateCategory(Category(id = 2, name = "bakery", colorHex = "#000000", aisleOrder = 3))

        val updated = repository.categories.value.single { it.id == 2L }
        assertEquals("bakery", updated.name)
        assertEquals("#000000", updated.colorHex)
    }

    @Test
    fun `update rejects renaming to another category's name`() {
        assertThrows(DuplicateCategoryNameException::class.java) {
            runBlocking {
                updateCategory(Category(id = 2, name = "Produce", colorHex = "#C98B4A", aisleOrder = 3))
            }
        }
        assertEquals("Bakery", repository.categories.value.single { it.id == 2L }.name)
    }

    @Test
    fun `delete removes the category`() = runBlocking {
        deleteCategory(2)

        assertTrue(repository.categories.value.none { it.id == 2L })
    }
}
