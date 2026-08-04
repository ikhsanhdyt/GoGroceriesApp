package com.diavolo.gogroceriesapp.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.usecase.CreateListUseCase
import com.diavolo.gogroceriesapp.domain.usecase.GetListsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeEvent {
    data object ListCreated : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getListsUseCase: GetListsUseCase,
    private val createListUseCase: CreateListUseCase
) : ViewModel() {

    private val creationState = MutableStateFlow(CreationState())
    private val retryTrigger = MutableStateFlow(0)
    private val eventChannel = Channel<HomeEvent>(Channel.BUFFERED)

    val events = eventChannel.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val listsState = retryTrigger.flatMapLatest {
        getListsUseCase()
            .map { lists -> ListsState(lists = lists) }
            .onStart { emit(ListsState(isLoading = true)) }
            .catch { emit(ListsState(loadError = "Couldn't load your lists.")) }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        listsState,
        creationState
    ) { lists, creation ->
        HomeUiState(
            isLoading = lists.isLoading,
            lists = lists.lists,
            isCreating = creation.isCreating,
            loadError = lists.loadError,
            creationError = creation.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    fun retryLoading() {
        retryTrigger.value += 1
    }

    fun clearCreationError() {
        creationState.value = creationState.value.copy(error = null)
    }

    fun createList(name: String, budgetRupiah: Long?) {
        if (name.isBlank() || creationState.value.isCreating) return

        viewModelScope.launch {
            creationState.value = CreationState(isCreating = true)
            runCatching {
                createListUseCase(name.trim(), budgetRupiah)
            }.onSuccess {
                creationState.value = CreationState()
                eventChannel.send(HomeEvent.ListCreated)
            }.onFailure {
                creationState.value = CreationState(
                    error = "Couldn't create the list. Please try again."
                )
            }
        }
    }

    private data class CreationState(
        val isCreating: Boolean = false,
        val error: String? = null
    )

    private data class ListsState(
        val isLoading: Boolean = false,
        val lists: List<GroceryList> = emptyList(),
        val loadError: String? = null
    )
}
