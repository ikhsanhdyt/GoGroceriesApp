package com.diavolo.gogroceriesapp.api.category.data.repository

import com.diavolo.gogroceriesapp.api.category.data.local.service.CategoryLocalDataSource
import com.diavolo.gogroceriesapp.api.category.data.mapper.toDomain
import com.diavolo.gogroceriesapp.api.category.data.mapper.toEntity
import com.diavolo.gogroceriesapp.core.entity.category.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val local: CategoryLocalDataSource
) : CategoryRepository {

    override fun observeCategories(): Flow<List<Category>> {
        return local.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertCategory(category: Category): Long =
        local.insert(category.toEntity())

    // Must stay an UPDATE: insert() uses REPLACE, which deletes the row first and makes the
    // ON DELETE SET_NULL foreign key detach every item from this category.
    override suspend fun updateCategory(category: Category) =
        local.update(category.toEntity())

    override suspend fun deleteCategory(id: Long) =
        local.delete(id)
}
