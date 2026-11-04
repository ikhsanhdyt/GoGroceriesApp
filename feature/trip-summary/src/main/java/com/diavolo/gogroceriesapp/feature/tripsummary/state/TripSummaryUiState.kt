package com.diavolo.gogroceriesapp.feature.tripsummary.state

import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryList
import com.diavolo.gogroceriesapp.core.entity.grocery.TripSummary

data class TripSummaryUiState(
    val isLoading: Boolean = true,
    val list: GroceryList? = null,
    val summary: TripSummary? = null,
    val isNotFound: Boolean = false,
    val isUnavailable: Boolean = false,
    val errorMessage: String? = null
)
