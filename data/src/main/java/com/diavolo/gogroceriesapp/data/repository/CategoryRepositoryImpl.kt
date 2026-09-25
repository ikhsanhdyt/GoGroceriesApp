package com.diavolo.gogroceriesapp.data.repository

import com.diavolo.gogroceriesapp.data.local.dao.CategoryDao
import com.diavolo.gogroceriesapp.data.mapper.toDomain
import com.diavolo.gogroceriesapp.data.mapper.toEntity
import com.diavolo.gogroceriesapp.domain.model.Category
import com.diavolo.gogroceriesapp.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun observeCategories(): Flow<List<Category>> {
        return categoryDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertCategory(category: Category): Long =
        categoryDao.insert(category.toEntity())

    // Must stay an UPDATE: insert() uses REPLACE, which deletes the row first and makes the
    // ON DELETE SET_NULL foreign key detach every item from this category.
    override suspend fun updateCategory(category: Category) =
        categoryDao.update(category.toEntity())

    override suspend fun deleteCategory(id: Long) =
        categoryDao.delete(id)
}
