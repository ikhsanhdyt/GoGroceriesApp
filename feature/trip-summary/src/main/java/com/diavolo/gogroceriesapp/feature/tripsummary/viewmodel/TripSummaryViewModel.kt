package com.diavolo.gogroceriesapp.feature.tripsummary.viewmodel

import androidx.lifecycle.viewModelScope
import com.diavolo.gogroceriesapp.api.grocerylist.domain.calculation.ComputeTripSummaryUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.get.GetListUseCase
import com.diavolo.gogroceriesapp.core.base.BaseState
import com.diavolo.gogroceriesapp.core.base.BaseViewModel
import com.diavolo.gogroceriesapp.core.base.asBaseState
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryList
import com.diavolo.gogroceriesapp.core.entity.grocery.ListStatus
import com.diavolo.gogroceriesapp.feature.tripsummary.state.TripSummaryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripSummaryViewModel @Inject constructor(
    private val getListUseCase: GetListUseCase,
    private val computeTripSummaryUseCase: ComputeTripSummaryUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(TripSummaryUiState())
    val uiState: StateFlow<TripSummaryUiState> = _uiState.asStateFlow()

    private var currentListId: Long? = null
    private var observationJob: Job? = null

    fun loadList(listId: Long) {
        if (currentListId == listId && observationJob?.isActive == true) return

        currentListId = listId
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            getListUseCase(listId)
                .asBaseState()
                .collect { state -> _uiState.value = state.toUiState() }
        }
    }

    fun retry() {
        currentListId?.let { listId ->
            currentListId = null
            loadList(listId)
        }
    }

    private fun BaseState<GroceryList?>.toUiState(): TripSummaryUiState = when (this) {
        BaseState.StateInitial, BaseState.StateLoading -> TripSummaryUiState(isLoading = true)
        is BaseState.StateFailed -> TripSummaryUiState(
            isLoading = false,
            errorMessage = "Couldn't load this trip summary."
        )
        is BaseState.StateSuccess -> {
            val list = data
            when {
                list == null -> TripSummaryUiState(isLoading = false, isNotFound = true)
                list.status != ListStatus.Completed -> TripSummaryUiState(
                    isLoading = false,
                    list = list,
                    isUnavailable = true
                )
                else -> TripSummaryUiState(
                    isLoading = false,
                    list = list,
                    summary = computeTripSummaryUseCase(list)
                )
            }
        }
    }
}
