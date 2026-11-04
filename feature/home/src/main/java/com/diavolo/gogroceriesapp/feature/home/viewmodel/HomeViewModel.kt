package com.diavolo.gogroceriesapp.feature.home.viewmodel

import androidx.lifecycle.viewModelScope
import com.diavolo.gogroceriesapp.api.grocerylist.domain.get.GetListsUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.list.CreateListUseCase
import com.diavolo.gogroceriesapp.core.base.BaseState.StateFailed
import com.diavolo.gogroceriesapp.core.base.BaseState.StateLoading
import com.diavolo.gogroceriesapp.core.base.BaseViewModel
import com.diavolo.gogroceriesapp.core.base.asBaseState
import com.diavolo.gogroceriesapp.feature.home.state.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

sealed interface HomeEvent {
    data object ListCreated : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getListsUseCase: GetListsUseCase,
    private val createListUseCase: CreateListUseCase
) : BaseViewModel() {

    private val creationState = MutableStateFlow(CreationState())
    private val retryTrigger = MutableStateFlow(0)
    private val eventChannel = Channel<HomeEvent>(Channel.BUFFERED)

    val events = eventChannel.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val listsState = retryTrigger.flatMapLatest { getListsUseCase().asBaseState() }

    val uiState: StateFlow<HomeUiState> = combine(
        listsState,
        creationState
    ) { lists, creation ->
        HomeUiState(
            isLoading = lists is StateLoading,
            lists = lists.data().orEmpty(),
            isCreating = creation.isCreating,
            loadError = if (lists is StateFailed) "Couldn't load your lists." else null,
            creationError = creation.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    fun retryLoading() {
        retryTrigger.update { it + 1 }
    }

    fun clearCreationError() {
        creationState.update { it.copy(error = null) }
    }

    fun createList(name: String, budgetRupiah: Long?) {
        if (name.isBlank() || creationState.value.isCreating) return

        creationState.value = CreationState(isCreating = true)
        launchSafeJob(
            block = { createListUseCase(name.trim(), budgetRupiah) },
            onSuccess = {
                creationState.value = CreationState()
                eventChannel.send(HomeEvent.ListCreated)
            },
            onError = {
                creationState.value = CreationState(
                    error = "Couldn't create the list. Please try again."
                )
            }
        )
    }

    private data class CreationState(
        val isCreating: Boolean = false,
        val error: String? = null
    )
}
