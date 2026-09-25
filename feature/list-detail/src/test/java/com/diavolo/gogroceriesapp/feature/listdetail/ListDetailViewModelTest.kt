package com.diavolo.gogroceriesapp.feature.listdetail

import com.diavolo.gogroceriesapp.domain.model.Category
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.model.ListStatus
import com.diavolo.gogroceriesapp.domain.model.UnitOfMeasure
import com.diavolo.gogroceriesapp.domain.repository.CategoryRepository
import com.diavolo.gogroceriesapp.domain.repository.GroceryListRepository
import com.diavolo.gogroceriesapp.domain.usecase.AddItemUseCase
import com.diavolo.gogroceriesapp.domain.usecase.ComputeEstimatedTotalUseCase
import com.diavolo.gogroceriesapp.domain.usecase.DeleteItemUseCase
import com.diavolo.gogroceriesapp.domain.usecase.GetCategoriesUseCase
import com.diavolo.gogroceriesapp.domain.usecase.GetListUseCase
import com.diavolo.gogroceriesapp.domain.usecase.StartShoppingUseCase
import com.diavolo.gogroceriesapp.domain.usecase.ToggleItemCheckedUseCase
import com.diavolo.gogroceriesapp.domain.usecase.UpdateItemUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListDetailViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeGroceryListRepository()
    private lateinit var viewModel: ListDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = ListDetailViewModel(
            getListUseCase = GetListUseCase(repository),
            getCategoriesUseCase = GetCategoriesUseCase(FakeCategoryRepository()),
            computeEstimatedTotal = ComputeEstimatedTotalUseCase(),
            addItemUseCase = AddItemUseCase(repository),
            toggleItemCheckedUseCase = ToggleItemCheckedUseCase(repository),
            updateItemUseCase = UpdateItemUseCase(repository),
            deleteItemUseCase = DeleteItemUseCase(repository),
            startShoppingUseCase = StartShoppingUseCase(repository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loading a list exposes its content`() = runTest(dispatcher) {
        repository.list.value = groceryList(name = "Weekly groceries")

        viewModel.loadList(LIST_ID)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Weekly groceries", state.list?.name)
    }

    @Test
    fun `adding flag survives a database emission while the add is in flight`() =
        runTest(dispatcher) {
            repository.list.value = groceryList(name = "Before")
            viewModel.loadList(LIST_ID)
            val gate = CompletableDeferred<Unit>()
            repository.addItemGate = gate

            viewModel.addItem("Milk", 1.0, UnitOfMeasure.PIECE, null, null)
            assertTrue(viewModel.uiState.value.isAddingItem)

            repository.list.value = groceryList(name = "After")

            assertEquals("After", viewModel.uiState.value.list?.name)
            assertTrue(viewModel.uiState.value.isAddingItem)

            gate.complete(Unit)
            assertFalse(viewModel.uiState.value.isAddingItem)
        }

    @Test
    fun `a finished add is not brought back by a later database emission`() =
        runTest(dispatcher) {
            repository.list.value = groceryList(name = "Before")
            viewModel.loadList(LIST_ID)

            viewModel.addItem("Milk", 1.0, UnitOfMeasure.PIECE, null, null)
            repository.list.value = groceryList(name = "After")

            assertFalse(viewModel.uiState.value.isAddingItem)
        }

    @Test
    fun `a second add is ignored while one is in flight`() = runTest(dispatcher) {
        repository.list.value = groceryList(name = "Weekly groceries")
        viewModel.loadList(LIST_ID)
        val gate = CompletableDeferred<Unit>()
        repository.addItemGate = gate

        viewModel.addItem("Milk", 1.0, UnitOfMeasure.PIECE, null, null)
        viewModel.addItem("Bread", 1.0, UnitOfMeasure.PIECE, null, null)
        gate.complete(Unit)

        assertEquals(listOf("Milk"), repository.addedItemNames)
    }

    @Test
    fun `a failed add shows an error and can be retried`() = runTest(dispatcher) {
        repository.list.value = groceryList(name = "Weekly groceries")
        viewModel.loadList(LIST_ID)
        repository.addItemFailure = IllegalStateException("disk full")

        viewModel.addItem("Milk", 1.0, UnitOfMeasure.PIECE, null, null)

        val state = viewModel.uiState.value
        assertFalse(state.isAddingItem)
        assertEquals("Couldn't add this item. Please try again.", state.addItemError)

        viewModel.clearAddItemError()
        assertNull(viewModel.uiState.value.addItemError)
    }

    @Test
    fun `a successful add emits the item added event`() = runTest(dispatcher) {
        repository.list.value = groceryList(name = "Weekly groceries")
        viewModel.loadList(LIST_ID)
        val events = mutableListOf<ListDetailEvent>()
        backgroundScope.launch { viewModel.events.toList(events) }

        viewModel.addItem("Milk", 1.0, UnitOfMeasure.PIECE, null, null)

        assertEquals(listOf<ListDetailEvent>(ListDetailEvent.ItemAdded), events)
    }

    @Test
    fun `updating flag for an item survives a database emission`() = runTest(dispatcher) {
        val item = groceryItem(id = 5, name = "Eggs")
        repository.list.value = groceryList(name = "Before", items = listOf(item))
        viewModel.loadList(LIST_ID)
        val gate = CompletableDeferred<Unit>()
        repository.toggleGate = gate

        viewModel.toggleItem(item)
        assertEquals(setOf(5L), viewModel.uiState.value.updatingItemIds)

        repository.list.value = groceryList(name = "After", items = listOf(item))
        assertEquals(setOf(5L), viewModel.uiState.value.updatingItemIds)

        gate.complete(Unit)
        assertTrue(viewModel.uiState.value.updatingItemIds.isEmpty())
    }

    private fun groceryList(
        name: String,
        items: List<GroceryItem> = emptyList()
    ) = GroceryList(
        id = LIST_ID,
        name = name,
        status = ListStatus.Draft,
        budgetRupiah = null,
        createdAt = 1,
        updatedAt = 1,
        items = items
    )

    private fun groceryItem(id: Long, name: String) = GroceryItem(
        id = id,
        listId = LIST_ID,
        categoryId = null,
        name = name,
        quantity = 1.0,
        unit = UnitOfMeasure.PIECE,
        estimatedPriceRupiah = null,
        actualPriceRupiah = null,
        isChecked = false,
        notes = null,
        position = 0
    )

    private class FakeGroceryListRepository : GroceryListRepository {
        val list = MutableStateFlow<GroceryList?>(null)
        val addedItemNames = mutableListOf<String>()
        var addItemGate: CompletableDeferred<Unit>? = null
        var addItemFailure: Throwable? = null
        var toggleGate: CompletableDeferred<Unit>? = null

        override fun observeLists(): Flow<List<GroceryList>> = emptyFlow()

        override fun observeListWithItems(id: Long): Flow<GroceryList?> = list

        override suspend fun create(name: String, budgetRupiah: Long?): Long = 1

        override suspend fun update(list: GroceryList) = Unit

        override suspend fun delete(id: Long) = Unit

        override suspend fun addItem(item: GroceryItem): Long {
            addedItemNames += item.name
            addItemGate?.await()
            addItemFailure?.let { throw it }
            return 1
        }

        override suspend fun updateItem(item: GroceryItem) = Unit

        override suspend fun toggleChecked(itemId: Long, checked: Boolean) {
            toggleGate?.await()
        }

        override suspend fun deleteItem(itemId: Long) = Unit
    }

    private class FakeCategoryRepository : CategoryRepository {
        override fun observeCategories(): Flow<List<Category>> = flowOf(emptyList())

        override suspend fun insertCategory(category: Category): Long = 1

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun deleteCategory(id: Long) = Unit
    }

    private companion object {
        const val LIST_ID = 1L
    }
}
