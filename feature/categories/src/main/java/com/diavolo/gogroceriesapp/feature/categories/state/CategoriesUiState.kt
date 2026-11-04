package com.diavolo.gogroceriesapp.feature.categories.state

import com.diavolo.gogroceriesapp.core.entity.category.Category

data class CategoriesUiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val loadError: String? = null,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val actionError: String? = null
)
