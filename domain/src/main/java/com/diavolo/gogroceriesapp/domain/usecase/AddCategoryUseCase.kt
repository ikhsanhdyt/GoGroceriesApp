package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.model.Category
import com.diavolo.gogroceriesapp.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    /**
     * Adds a category after the existing ones.
     *
     * @throws IllegalArgumentException if [name] is blank.
     * @throws DuplicateCategoryNameException if another category already uses [name].
     */
    suspend operator fun invoke(name: String, colorHex: String): Long {
        val existing = repository.observeCategories().first()
        val trimmedName = validateCategoryName(name, existing)
        return repository.insertCategory(
            Category(
                name = trimmedName,
                colorHex = colorHex,
                aisleOrder = (existing.maxOfOrNull { it.aisleOrder } ?: -1) + 1
            )
        )
    }
}
