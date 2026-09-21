package com.diavolo.gogroceriesapp.feature.home

import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.model.ListStatus
import com.diavolo.gogroceriesapp.domain.repository.GroceryListRepository
import com.diavolo.gogroceriesapp.domain.usecase.CreateListUseCase
import com.diavolo.gogroceriesapp.domain.usecase.GetListsUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
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
class HomeViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeGroceryListRepository()
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = HomeViewModel(
            getListsUseCase = GetListsUseCase(repository),
            createListUseCase = CreateListUseCase(repository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `lists from the repository are exposed`() = runTest(dispatcher) {
        repository.lists.value = listOf(groceryList(id = 1, name = "Weekly groceries"))
        backgroundScope.launch { viewModel.uiState.collect { } }

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals(listOf("Weekly groceries"), state.lists.map { it.name })
    }

    @Test
    fun `creating a list emits the created event and clears the creating flag`() =
        runTest(dispatcher) {
            backgroundScope.launch { viewModel.uiState.collect { } }
            val events = mutableListOf<HomeEvent>()
            backgroundScope.launch { viewModel.events.toList(events) }

            viewModel.createList("  Weekly groceries  ", 250_000)

            assertEquals(listOf<HomeEvent>(HomeEvent.ListCreated), events)
            assertEquals(listOf("Weekly groceries" to 250_000L), repository.created)
            assertFalse(viewModel.uiState.value.isCreating)
            assertNull(viewModel.uiState.value.creationError)
        }

    @Test
    fun `a second create is ignored while one is in flight`() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect { } }
        val gate = CompletableDeferred<Unit>()
        repository.createGate = gate

        viewModel.createList("First", null)
        assertTrue(viewModel.uiState.value.isCreating)
        viewModel.createList("Second", null)
        gate.complete(Unit)

        assertEquals(listOf("First"), repository.created.map { it.first })
    }

    @Test
    fun `a failed create shows an error that can be cleared`() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect { } }
        repository.createFailure = IllegalStateException("disk full")

        viewModel.createList("Weekly groceries", null)

        assertFalse(viewModel.uiState.value.isCreating)
        assertEquals(
            "Couldn't create the list. Please try again.",
            viewModel.uiState.value.creationError
        )

        viewModel.clearCreationError()
        assertNull(viewModel.uiState.value.creationError)
    }

    @Test
    fun `a blank name is not created`() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect { } }

        viewModel.createList("   ", null)

        assertTrue(repository.created.isEmpty())
    }

    @Test
    fun `retrying reloads the lists`() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect { } }
        assertEquals(1, repository.observeCalls)

        viewModel.retryLoading()

        assertEquals(2, repository.observeCalls)
    }

    private fun groceryList(id: Long, name: String) = GroceryList(
        id = id,
        name = name,
        status = ListStatus.Draft,
        budgetRupiah = null,
        createdAt = 1,
        updatedAt = 1
    )

    private class FakeGroceryListRepository : GroceryListRepository {
        val lists = MutableStateFlow<List<GroceryList>>(emptyList())
        val created = mutableListOf<Pair<String, Long?>>()
        var observeCalls = 0
        var createGate: CompletableDeferred<Unit>? = null
        var createFailure: Throwable? = null

        override fun observeLists(): Flow<List<GroceryList>> {
            observeCalls++
            return lists
        }

        override fun observeListWithItems(id: Long): Flow<GroceryList?> = emptyFlow()

        override suspend fun create(name: String, budgetRupiah: Long?): Long {
            created += name to budgetRupiah
            createGate?.await()
            createFailure?.let { throw it }
            return 1
        }

        override suspend fun update(list: GroceryList) = Unit

        override suspend fun delete(id: Long) = Unit

        override suspend fun addItem(item: GroceryItem): Long = 1

        override suspend fun updateItem(item: GroceryItem) = Unit

        override suspend fun toggleChecked(itemId: Long, checked: Boolean) = Unit

        override suspend fun deleteItem(itemId: Long) = Unit
    }
}
