package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.model.Category
import com.diavolo.gogroceriesapp.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    /**
     * @throws IllegalArgumentException if the new name is blank.
     * @throws DuplicateCategoryNameException if another category already uses the new name.
     */
    suspend operator fun invoke(category: Category) {
        val others = repository.observeCategories().first().filter { it.id != category.id }
        val trimmedName = validateCategoryName(category.name, others)
        repository.updateCategory(category.copy(name = trimmedName))
    }
}
