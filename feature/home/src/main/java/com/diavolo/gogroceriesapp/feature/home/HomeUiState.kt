package com.diavolo.gogroceriesapp.feature.home

import com.diavolo.gogroceriesapp.domain.model.GroceryList

data class HomeUiState(
    val isLoading: Boolean = true,
    val lists: List<GroceryList> = emptyList(),
    val isCreating: Boolean = false,
    val loadError: String? = null,
    val creationError: String? = null
)
