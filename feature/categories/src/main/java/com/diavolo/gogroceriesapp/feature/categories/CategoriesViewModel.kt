package com.diavolo.gogroceriesapp.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diavolo.gogroceriesapp.core.util.suspendRunCatching
import com.diavolo.gogroceriesapp.domain.model.Category
import com.diavolo.gogroceriesapp.domain.usecase.AddCategoryUseCase
import com.diavolo.gogroceriesapp.domain.usecase.DeleteCategoryUseCase
import com.diavolo.gogroceriesapp.domain.usecase.DuplicateCategoryNameException
import com.diavolo.gogroceriesapp.domain.usecase.GetCategoriesUseCase
import com.diavolo.gogroceriesapp.domain.usecase.UpdateCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CategoriesEvent {
    data object CategorySaved : CategoriesEvent
    data object CategoryDeleted : CategoriesEvent
}

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val actionState = MutableStateFlow(ActionState())
    private val retryTrigger = MutableStateFlow(0)
    private val eventChannel = Channel<CategoriesEvent>(Channel.BUFFERED)

    val events = eventChannel.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val categoriesState = retryTrigger.flatMapLatest {
        getCategoriesUseCase()
            .map { categories -> CategoriesState(categories = categories) }
            .onStart { emit(CategoriesState(isLoading = true)) }
            .catch { emit(CategoriesState(loadError = "Couldn't load your categories.")) }
    }

    val uiState: StateFlow<CategoriesUiState> = combine(
        categoriesState,
        actionState
    ) { categories, action ->
        CategoriesUiState(
            isLoading = categories.isLoading,
            categories = categories.categories,
            loadError = categories.loadError,
            isSaving = action.isSaving,
            isDeleting = action.isDeleting,
            actionError = action.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoriesUiState()
    )

    fun retryLoading() {
        retryTrigger.update { it + 1 }
    }

    fun clearActionError() {
        actionState.update { it.copy(error = null) }
    }

    fun addCategory(name: String, colorHex: String) {
        save { addCategoryUseCase(name, colorHex) }
    }

    fun updateCategory(category: Category) {
        save { updateCategoryUseCase(category) }
    }

    fun deleteCategory(id: Long) {
        if (actionState.value.isBusy) return

        viewModelScope.launch {
            actionState.value = ActionState(isDeleting = true)
            suspendRunCatching {
                deleteCategoryUseCase(id)
            }.onSuccess {
                actionState.value = ActionState()
                eventChannel.send(CategoriesEvent.CategoryDeleted)
            }.onFailure {
                actionState.value = ActionState(
                    error = "Couldn't delete the category. Please try again."
                )
            }
        }
    }

    private fun save(block: suspend () -> Unit) {
        if (actionState.value.isBusy) return

        viewModelScope.launch {
            actionState.value = ActionState(isSaving = true)
            suspendRunCatching {
                block()
            }.onSuccess {
                actionState.value = ActionState()
                eventChannel.send(CategoriesEvent.CategorySaved)
            }.onFailure { error ->
                actionState.value = ActionState(
                    error = when (error) {
                        is DuplicateCategoryNameException -> "A category with this name already exists."
                        is IllegalArgumentException -> "Enter a name for the category."
                        else -> "Couldn't save the category. Please try again."
                    }
                )
            }
        }
    }

    private data class ActionState(
        val isSaving: Boolean = false,
        val isDeleting: Boolean = false,
        val error: String? = null
    ) {
        val isBusy: Boolean get() = isSaving || isDeleting
    }

    private data class CategoriesState(
        val isLoading: Boolean = false,
        val categories: List<Category> = emptyList(),
        val loadError: String? = null
    )
}
