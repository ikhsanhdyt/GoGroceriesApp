package com.diavolo.gogroceriesapp.feature.analytics.state

import com.diavolo.gogroceriesapp.core.entity.analytics.BudgetAnalytics

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val analytics: BudgetAnalytics? = null,
    val errorMessage: String? = null
)
