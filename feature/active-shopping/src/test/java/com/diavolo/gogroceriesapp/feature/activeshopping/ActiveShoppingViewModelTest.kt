package com.diavolo.gogroceriesapp.feature.activeshopping

import com.diavolo.gogroceriesapp.core.util.TimeProvider
import com.diavolo.gogroceriesapp.domain.model.Category
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.model.ListStatus
import com.diavolo.gogroceriesapp.domain.model.UnitOfMeasure
import com.diavolo.gogroceriesapp.domain.repository.CategoryRepository
import com.diavolo.gogroceriesapp.domain.repository.GroceryListRepository
import com.diavolo.gogroceriesapp.domain.usecase.ComputeActualTotalUseCase
import com.diavolo.gogroceriesapp.domain.usecase.ComputeEstimatedTotalUseCase
import com.diavolo.gogroceriesapp.domain.usecase.FinishShoppingUseCase
import com.diavolo.gogroceriesapp.domain.usecase.GetCategoriesUseCase
import com.diavolo.gogroceriesapp.domain.usecase.GetListUseCase
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
class ActiveShoppingViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeGroceryListRepository()
    private lateinit var viewModel: ActiveShoppingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = ActiveShoppingViewModel(
            getListUseCase = GetListUseCase(repository),
            getCategoriesUseCase = GetCategoriesUseCase(FakeCategoryRepository()),
            computeEstimatedTotalUseCase = ComputeEstimatedTotalUseCase(),
            computeActualTotalUseCase = ComputeActualTotalUseCase(),
            toggleItemCheckedUseCase = ToggleItemCheckedUseCase(repository),
            finishShoppingUseCase = FinishShoppingUseCase(repository, FixedTimeProvider),
            updateItemUseCase = UpdateItemUseCase(repository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loading a list exposes its content`() = runTest(dispatcher) {
        repository.list.value = shoppingList(name = "Weekly groceries")

        viewModel.loadList(LIST_ID)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Weekly groceries", state.list?.name)
    }

    @Test
    fun `updating flag survives the database emission caused by the toggle itself`() =
        runTest(dispatcher) {
            val item = shoppingItem(id = 5, name = "Eggs")
            repository.list.value = shoppingList(name = "Before", items = listOf(item))
            viewModel.loadList(LIST_ID)
            val gate = CompletableDeferred<Unit>()
            repository.toggleGate = gate

            viewModel.toggleItem(item)
            assertEquals(setOf(5L), viewModel.uiState.value.updatingItemIds)

            // Room re-emits while the write is still in flight.
            repository.list.value = shoppingList(
                name = "After",
                items = listOf(item.copy(isChecked = true))
            )
            assertEquals(setOf(5L), viewModel.uiState.value.updatingItemIds)

            gate.complete(Unit)
            assertTrue(viewModel.uiState.value.updatingItemIds.isEmpty())
        }

    @Test
    fun `updating flag is not brought back by an emission after the toggle finished`() =
        runTest(dispatcher) {
            val item = shoppingItem(id = 5, name = "Eggs")
            repository.list.value = shoppingList(name = "Before", items = listOf(item))
            viewModel.loadList(LIST_ID)

            viewModel.toggleItem(item)
            repository.list.value = shoppingList(
                name = "After",
                items = listOf(item.copy(isChecked = true))
            )

            assertTrue(viewModel.uiState.value.updatingItemIds.isEmpty())
        }

    @Test
    fun `a second toggle of the same item is ignored while one is in flight`() =
        runTest(dispatcher) {
            val item = shoppingItem(id = 5, name = "Eggs")
            repository.list.value = shoppingList(name = "Weekly groceries", items = listOf(item))
            viewModel.loadList(LIST_ID)
            val gate = CompletableDeferred<Unit>()
            repository.toggleGate = gate

            viewModel.toggleItem(item)
            viewModel.toggleItem(item)
            gate.complete(Unit)

            assertEquals(1, repository.toggleCalls)
        }

    @Test
    fun `price error survives a database emission and can be cleared`() = runTest(dispatcher) {
        val item = shoppingItem(id = 5, name = "Eggs")
        repository.list.value = shoppingList(name = "Before", items = listOf(item))
        viewModel.loadList(LIST_ID)
        repository.updateItemFailure = IllegalStateException("disk full")

        viewModel.updateActualPrice(item, 12_000)
        val expected = "Couldn't save the actual price. Please try again."
        assertEquals(expected, viewModel.uiState.value.priceUpdateError)

        repository.list.value = shoppingList(name = "After", items = listOf(item))
        assertEquals(expected, viewModel.uiState.value.priceUpdateError)

        viewModel.clearPriceUpdateError()
        assertNull(viewModel.uiState.value.priceUpdateError)
    }

    @Test
    fun `a successful price update emits the price updated event`() = runTest(dispatcher) {
        val item = shoppingItem(id = 5, name = "Eggs")
        repository.list.value = shoppingList(name = "Weekly groceries", items = listOf(item))
        viewModel.loadList(LIST_ID)
        val events = mutableListOf<ActiveShoppingEvent>()
        backgroundScope.launch { viewModel.events.toList(events) }

        viewModel.updateActualPrice(item, 12_000)

        assertEquals(listOf<ActiveShoppingEvent>(ActiveShoppingEvent.PriceUpdated), events)
    }

    @Test
    fun `finishing completes the list and emits the finished event`() = runTest(dispatcher) {
        repository.list.value = shoppingList(
            name = "Weekly groceries",
            status = ListStatus.Active,
            items = listOf(shoppingItem(id = 5, name = "Eggs"))
        )
        viewModel.loadList(LIST_ID)
        val events = mutableListOf<ActiveShoppingEvent>()
        backgroundScope.launch { viewModel.events.toList(events) }

        viewModel.finishShopping()

        assertEquals(listOf<ActiveShoppingEvent>(ActiveShoppingEvent.ShoppingFinished), events)
        assertEquals(ListStatus.Completed, repository.updatedList?.status)
        assertEquals(FixedTimeProvider.nowMillis(), repository.updatedList?.completedAt)
        assertFalse(viewModel.uiState.value.isFinishing)
    }

    @Test
    fun `finishing flag survives a database emission while finishing`() = runTest(dispatcher) {
        repository.list.value = shoppingList(
            name = "Before",
            status = ListStatus.Active,
            items = listOf(shoppingItem(id = 5, name = "Eggs"))
        )
        viewModel.loadList(LIST_ID)
        val gate = CompletableDeferred<Unit>()
        repository.updateGate = gate

        viewModel.finishShopping()
        assertTrue(viewModel.uiState.value.isFinishing)

        repository.list.value = shoppingList(
            name = "After",
            status = ListStatus.Active,
            items = listOf(shoppingItem(id = 5, name = "Eggs"))
        )
        assertTrue(viewModel.uiState.value.isFinishing)

        gate.complete(Unit)
        assertFalse(viewModel.uiState.value.isFinishing)
    }

    private fun shoppingList(
        name: String,
        status: ListStatus = ListStatus.Active,
        items: List<GroceryItem> = emptyList()
    ) = GroceryList(
        id = LIST_ID,
        name = name,
        status = status,
        budgetRupiah = null,
        createdAt = 1,
        updatedAt = 1,
        items = items
    )

    private fun shoppingItem(id: Long, name: String) = GroceryItem(
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

    private object FixedTimeProvider : TimeProvider {
        override fun nowMillis(): Long = 9_000L
    }

    private class FakeGroceryListRepository : GroceryListRepository {
        val list = MutableStateFlow<GroceryList?>(null)
        var updatedList: GroceryList? = null
        var toggleCalls = 0
        var toggleGate: CompletableDeferred<Unit>? = null
        var updateGate: CompletableDeferred<Unit>? = null
        var updateItemFailure: Throwable? = null

        override fun observeLists(): Flow<List<GroceryList>> = emptyFlow()

        override fun observeListWithItems(id: Long): Flow<GroceryList?> = list

        override suspend fun create(name: String, budgetRupiah: Long?): Long = 1

        override suspend fun update(list: GroceryList) {
            updateGate?.await()
            updatedList = list
        }

        override suspend fun delete(id: Long) = Unit

        override suspend fun addItem(item: GroceryItem): Long = 1

        override suspend fun updateItem(item: GroceryItem) {
            updateItemFailure?.let { throw it }
        }

        override suspend fun toggleChecked(itemId: Long, checked: Boolean) {
            toggleCalls++
            toggleGate?.await()
        }

        override suspend fun deleteItem(itemId: Long) = Unit
    }

    private class FakeCategoryRepository : CategoryRepository {
        override fun observeCategories(): Flow<List<Category>> = flowOf(emptyList())

        override suspend fun insertCategory(category: Category): Long = 1
    }

    private companion object {
        const val LIST_ID = 1L
    }
}
