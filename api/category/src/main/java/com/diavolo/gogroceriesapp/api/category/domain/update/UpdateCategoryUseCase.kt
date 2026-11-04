package com.diavolo.gogroceriesapp.api.category.domain.update

import com.diavolo.gogroceriesapp.api.category.domain.validation.DuplicateCategoryNameException
import com.diavolo.gogroceriesapp.core.entity.category.Category

interface UpdateCategoryUseCase {
    /**
     * @throws IllegalArgumentException if the new name is blank.
     * @throws DuplicateCategoryNameException if another category already uses the new name.
     */
    suspend operator fun invoke(category: Category)
}
