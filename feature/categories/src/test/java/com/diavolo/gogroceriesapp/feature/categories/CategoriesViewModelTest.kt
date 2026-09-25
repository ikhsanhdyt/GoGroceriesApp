package com.diavolo.gogroceriesapp.feature.categories

import com.diavolo.gogroceriesapp.domain.model.Category
import com.diavolo.gogroceriesapp.domain.repository.CategoryRepository
import com.diavolo.gogroceriesapp.domain.usecase.AddCategoryUseCase
import com.diavolo.gogroceriesapp.domain.usecase.DeleteCategoryUseCase
import com.diavolo.gogroceriesapp.domain.usecase.GetCategoriesUseCase
import com.diavolo.gogroceriesapp.domain.usecase.UpdateCategoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeCategoryRepository(
        listOf(
            Category(id = 1, name = "Produce", colorHex = "#4F8A5B", aisleOrder = 0),
            Category(id = 2, name = "Bakery", colorHex = "#C98B4A", aisleOrder = 1)
        )
    )
    private lateinit var viewModel: CategoriesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = CategoriesViewModel(
            getCategoriesUseCase = GetCategoriesUseCase(repository),
            addCategoryUseCase = AddCategoryUseCase(repository),
            updateCategoryUseCase = UpdateCategoryUseCase(repository),
            deleteCategoryUseCase = DeleteCategoryUseCase(repository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `categories are exposed once loaded`() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf("Produce", "Bakery"), state.categories.map { it.name })
    }

    @Test
    fun `adding a category saves it and emits CategorySaved`() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.addCategory("Drinks", "#2E7D6B")

        assertEquals(CategoriesEvent.CategorySaved, viewModel.events.first())
        assertEquals(
            listOf("Produce", "Bakery", "Drinks"),
            viewModel.uiState.value.categories.map { it.name }
        )
        assertNull(viewModel.uiState.value.actionError)
    }

    @Test
    fun `adding a duplicate name shows an error and saves nothing`() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.addCategory("bakery", "#2E7D6B")

        val state = viewModel.uiState.value
        assertEquals("A category with this name already exists.", state.actionError)
        assertFalse(state.isSaving)
        assertEquals(2, state.categories.size)
    }

    @Test
    fun `updating keeps the id and aisle order`() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }
        val bakery = viewModel.uiState.value.categories.single { it.id == 2L }

        viewModel.updateCategory(bakery.copy(name = "Roti", colorHex = "#6B7280"))

        assertEquals(CategoriesEvent.CategorySaved, viewModel.events.first())
        assertEquals(
            Category(id = 2, name = "Roti", colorHex = "#6B7280", aisleOrder = 1),
            viewModel.uiState.value.categories.single { it.id == 2L }
        )
    }

    @Test
    fun `deleting removes the category and emits CategoryDeleted`() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.deleteCategory(1)

        assertEquals(CategoriesEvent.CategoryDeleted, viewModel.events.first())
        assertEquals(listOf("Bakery"), viewModel.uiState.value.categories.map { it.name })
    }

    @Test
    fun `a failed delete shows an error`() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }
        repository.failDeletes = true

        viewModel.deleteCategory(1)

        val state = viewModel.uiState.value
        assertEquals("Couldn't delete the category. Please try again.", state.actionError)
        assertFalse(state.isDeleting)
        assertEquals(2, state.categories.size)
    }

    private class FakeCategoryRepository(initial: List<Category>) : CategoryRepository {
        val categories = MutableStateFlow(initial)
        var failDeletes = false

        override fun observeCategories(): Flow<List<Category>> = categories

        override suspend fun insertCategory(category: Category): Long {
            val id = (categories.value.maxOfOrNull { it.id } ?: 0) + 1
            categories.value = categories.value + category.copy(id = id)
            return id
        }

        override suspend fun updateCategory(category: Category) {
            categories.value = categories.value.map { if (it.id == category.id) category else it }
        }

        override suspend fun deleteCategory(id: Long) {
            if (failDeletes) error("Database unavailable")
            categories.value = categories.value.filterNot { it.id == id }
        }
    }
}
