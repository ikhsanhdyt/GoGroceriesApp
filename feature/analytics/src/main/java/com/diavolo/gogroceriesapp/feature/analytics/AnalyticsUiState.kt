package com.diavolo.gogroceriesapp.feature.analytics

import com.diavolo.gogroceriesapp.domain.model.BudgetAnalytics

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val analytics: BudgetAnalytics? = null,
    val errorMessage: String? = null
)
