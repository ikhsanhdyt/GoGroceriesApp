package com.diavolo.gogroceriesapp.feature.tripsummary

import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.model.TripSummary

data class TripSummaryUiState(
    val isLoading: Boolean = true,
    val list: GroceryList? = null,
    val summary: TripSummary? = null,
    val isNotFound: Boolean = false,
    val isUnavailable: Boolean = false,
    val errorMessage: String? = null
)
