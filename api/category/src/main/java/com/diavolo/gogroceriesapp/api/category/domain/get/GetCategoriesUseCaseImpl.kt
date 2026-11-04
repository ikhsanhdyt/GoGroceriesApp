package com.diavolo.gogroceriesapp.api.category.domain.get

import com.diavolo.gogroceriesapp.api.category.data.repository.CategoryRepository
import javax.inject.Inject

class GetCategoriesUseCaseImpl @Inject constructor(
    private val repository: CategoryRepository
) : GetCategoriesUseCase {

    override fun invoke() = repository.observeCategories()
}
