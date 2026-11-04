package com.diavolo.gogroceriesapp.api.category.domain.add

import com.diavolo.gogroceriesapp.api.category.data.repository.CategoryRepository
import com.diavolo.gogroceriesapp.api.category.domain.validation.validateCategoryName
import com.diavolo.gogroceriesapp.core.entity.category.Category
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddCategoryUseCaseImpl @Inject constructor(
    private val repository: CategoryRepository
) : AddCategoryUseCase {

    override suspend fun invoke(name: String, colorHex: String): Long {
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
