package com.diavolo.gogroceriesapp.feature.categories.viewmodel

import androidx.lifecycle.viewModelScope
import com.diavolo.gogroceriesapp.api.category.domain.add.AddCategoryUseCase
import com.diavolo.gogroceriesapp.api.category.domain.delete.DeleteCategoryUseCase
import com.diavolo.gogroceriesapp.api.category.domain.get.GetCategoriesUseCase
import com.diavolo.gogroceriesapp.api.category.domain.update.UpdateCategoryUseCase
import com.diavolo.gogroceriesapp.api.category.domain.validation.DuplicateCategoryNameException
import com.diavolo.gogroceriesapp.core.base.BaseState.StateFailed
import com.diavolo.gogroceriesapp.core.base.BaseState.StateLoading
import com.diavolo.gogroceriesapp.core.base.BaseViewModel
import com.diavolo.gogroceriesapp.core.base.asBaseState
import com.diavolo.gogroceriesapp.core.entity.category.Category
import com.diavolo.gogroceriesapp.feature.categories.state.CategoriesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
) : BaseViewModel() {

    private val actionState = MutableStateFlow(ActionState())
    private val retryTrigger = MutableStateFlow(0)
    private val eventChannel = Channel<CategoriesEvent>(Channel.BUFFERED)

    val events = eventChannel.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val categoriesState = retryTrigger.flatMapLatest {
        getCategoriesUseCase().asBaseState()
    }

    val uiState: StateFlow<CategoriesUiState> = combine(
        categoriesState,
        actionState
    ) { categories, action ->
        CategoriesUiState(
            isLoading = categories is StateLoading,
            categories = categories.data().orEmpty(),
            loadError = if (categories is StateFailed) "Couldn't load your categories." else null,
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

        actionState.value = ActionState(isDeleting = true)
        launchSafeJob(
            block = { deleteCategoryUseCase(id) },
            onSuccess = {
                actionState.value = ActionState()
                eventChannel.send(CategoriesEvent.CategoryDeleted)
            },
            onError = {
                actionState.value = ActionState(
                    error = "Couldn't delete the category. Please try again."
                )
            }
        )
    }

    private fun save(block: suspend () -> Unit) {
        if (actionState.value.isBusy) return

        actionState.value = ActionState(isSaving = true)
        launchSafeJob(
            block = block,
            onSuccess = {
                actionState.value = ActionState()
                eventChannel.send(CategoriesEvent.CategorySaved)
            },
            onError = { error ->
                actionState.value = ActionState(
                    error = when (error) {
                        is DuplicateCategoryNameException -> "A category with this name already exists."
                        is IllegalArgumentException -> "Enter a name for the category."
                        else -> "Couldn't save the category. Please try again."
                    }
                )
            }
        )
    }

    private data class ActionState(
        val isSaving: Boolean = false,
        val isDeleting: Boolean = false,
        val error: String? = null
    ) {
        val isBusy: Boolean get() = isSaving || isDeleting
    }
}
