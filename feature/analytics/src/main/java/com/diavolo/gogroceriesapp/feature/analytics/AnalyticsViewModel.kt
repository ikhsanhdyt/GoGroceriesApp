package com.diavolo.gogroceriesapp.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diavolo.gogroceriesapp.domain.usecase.ComputeBudgetAnalyticsUseCase
import com.diavolo.gogroceriesapp.domain.usecase.GetCategoriesUseCase
import com.diavolo.gogroceriesapp.domain.usecase.GetListsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getListsUseCase: GetListsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val computeBudgetAnalyticsUseCase: ComputeBudgetAnalyticsUseCase
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AnalyticsUiState> = retryTrigger
        .flatMapLatest {
            combine(
                getListsUseCase(),
                getCategoriesUseCase()
            ) { lists, categories ->
                AnalyticsUiState(
                    isLoading = false,
                    analytics = computeBudgetAnalyticsUseCase(lists, categories)
                )
            }
                .onStart { emit(AnalyticsUiState(isLoading = true)) }
                .catch {
                    emit(
                        AnalyticsUiState(
                            isLoading = false,
                            errorMessage = "Couldn't load budget analytics."
                        )
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AnalyticsUiState()
        )

    fun retry() {
        retryTrigger.value += 1
    }
}
