package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.repository.CategoryRepository
import javax.inject.Inject

/** Items that used the deleted category keep existing and fall back to no category. */
class DeleteCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteCategory(id)
    }
}
