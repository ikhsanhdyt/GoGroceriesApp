package com.diavolo.gogroceriesapp.feature.analytics.viewmodel

import androidx.lifecycle.viewModelScope
import com.diavolo.gogroceriesapp.api.category.domain.get.GetCategoriesUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.calculation.ComputeBudgetAnalyticsUseCase
import com.diavolo.gogroceriesapp.api.grocerylist.domain.get.GetListsUseCase
import com.diavolo.gogroceriesapp.core.base.BaseState.StateFailed
import com.diavolo.gogroceriesapp.core.base.BaseState.StateLoading
import com.diavolo.gogroceriesapp.core.base.BaseViewModel
import com.diavolo.gogroceriesapp.core.base.asBaseState
import com.diavolo.gogroceriesapp.feature.analytics.state.AnalyticsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getListsUseCase: GetListsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val computeBudgetAnalyticsUseCase: ComputeBudgetAnalyticsUseCase
) : BaseViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AnalyticsUiState> = retryTrigger
        .flatMapLatest {
            combine(
                getListsUseCase(),
                getCategoriesUseCase()
            ) { lists, categories ->
                computeBudgetAnalyticsUseCase(lists, categories)
            }.asBaseState()
        }
        .map { state ->
            AnalyticsUiState(
                isLoading = state is StateLoading,
                analytics = state.data(),
                errorMessage = if (state is StateFailed) "Couldn't load budget analytics." else null
            )
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
