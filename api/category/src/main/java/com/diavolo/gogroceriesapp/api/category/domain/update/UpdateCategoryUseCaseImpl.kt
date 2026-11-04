package com.diavolo.gogroceriesapp.api.category.domain.update

import com.diavolo.gogroceriesapp.api.category.data.repository.CategoryRepository
import com.diavolo.gogroceriesapp.api.category.domain.validation.validateCategoryName
import com.diavolo.gogroceriesapp.core.entity.category.Category
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateCategoryUseCaseImpl @Inject constructor(
    private val repository: CategoryRepository
) : UpdateCategoryUseCase {

    override suspend fun invoke(category: Category) {
        val others = repository.observeCategories().first().filter { it.id != category.id }
        val trimmedName = validateCategoryName(category.name, others)
        repository.updateCategory(category.copy(name = trimmedName))
    }
}
