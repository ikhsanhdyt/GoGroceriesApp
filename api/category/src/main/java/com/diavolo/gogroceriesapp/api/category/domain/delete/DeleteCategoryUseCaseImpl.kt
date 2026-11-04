package com.diavolo.gogroceriesapp.api.category.domain.delete

import com.diavolo.gogroceriesapp.api.category.data.repository.CategoryRepository
import javax.inject.Inject

class DeleteCategoryUseCaseImpl @Inject constructor(
    private val repository: CategoryRepository
) : DeleteCategoryUseCase {

    override suspend fun invoke(id: Long) = repository.deleteCategory(id)
}
