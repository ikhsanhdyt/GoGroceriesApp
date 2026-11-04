package com.diavolo.gogroceriesapp.feature.home.state

import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryList

data class HomeUiState(
    val isLoading: Boolean = true,
    val lists: List<GroceryList> = emptyList(),
    val isCreating: Boolean = false,
    val loadError: String? = null,
    val creationError: String? = null
)
