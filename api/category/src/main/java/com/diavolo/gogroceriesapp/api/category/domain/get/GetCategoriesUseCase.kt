package com.diavolo.gogroceriesapp.api.category.domain.get

import com.diavolo.gogroceriesapp.core.entity.category.Category
import kotlinx.coroutines.flow.Flow

interface GetCategoriesUseCase {
    operator fun invoke(): Flow<List<Category>>
}
