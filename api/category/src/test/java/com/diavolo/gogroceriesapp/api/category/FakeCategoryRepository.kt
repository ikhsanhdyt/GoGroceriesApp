package com.diavolo.gogroceriesapp.api.category

import com.diavolo.gogroceriesapp.api.category.data.repository.CategoryRepository
import com.diavolo.gogroceriesapp.core.entity.category.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCategoryRepository(initial: List<Category> = emptyList()) : CategoryRepository {

    val categories = MutableStateFlow(initial)

    override fun observeCategories(): Flow<List<Category>> = categories

    override suspend fun insertCategory(category: Category): Long {
        val id = (categories.value.maxOfOrNull { it.id } ?: 0) + 1
        categories.value = categories.value + category.copy(id = id)
        return id
    }

    override suspend fun updateCategory(category: Category) {
        categories.value = categories.value.map { if (it.id == category.id) category else it }
    }

    override suspend fun deleteCategory(id: Long) {
        categories.value = categories.value.filterNot { it.id == id }
    }
}
