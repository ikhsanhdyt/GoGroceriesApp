package com.diavolo.gogroceriesapp.feature.tripsummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diavolo.gogroceriesapp.domain.model.ListStatus
import com.diavolo.gogroceriesapp.domain.usecase.ComputeTripSummaryUseCase
import com.diavolo.gogroceriesapp.domain.usecase.GetListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripSummaryViewModel @Inject constructor(
    private val getListUseCase: GetListUseCase,
    private val computeTripSummaryUseCase: ComputeTripSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripSummaryUiState())
    val uiState: StateFlow<TripSummaryUiState> = _uiState.asStateFlow()

    private var currentListId: Long? = null
    private var observationJob: Job? = null

    fun loadList(listId: Long) {
        if (currentListId == listId && observationJob?.isActive == true) return

        currentListId = listId
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            _uiState.value = TripSummaryUiState(isLoading = true)
            getListUseCase(listId)
                .catch {
                    _uiState.value = TripSummaryUiState(
                        isLoading = false,
                        errorMessage = "Couldn't load this trip summary."
                    )
                }
                .collect { list ->
                    _uiState.value = when {
                        list == null -> TripSummaryUiState(
                            isLoading = false,
                            isNotFound = true
                        )
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

    fun retry() {
        currentListId?.let { listId ->
            currentListId = null
            loadList(listId)
        }
    }
}
