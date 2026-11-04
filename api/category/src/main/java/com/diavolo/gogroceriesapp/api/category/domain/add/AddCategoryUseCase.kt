package com.diavolo.gogroceriesapp.api.category.domain.add

import com.diavolo.gogroceriesapp.api.category.domain.validation.DuplicateCategoryNameException

interface AddCategoryUseCase {
    /**
     * Adds a category after the existing ones.
     *
     * @throws IllegalArgumentException if [name] is blank.
     * @throws DuplicateCategoryNameException if another category already uses [name].
     */
    suspend operator fun invoke(name: String, colorHex: String): Long
}
