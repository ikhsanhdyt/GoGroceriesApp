package com.diavolo.gogroceriesapp.api.category.data.repository

import com.diavolo.gogroceriesapp.core.entity.category.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun observeCategories(): Flow<List<Category>>

    suspend fun insertCategory(category: Category): Long

    suspend fun updateCategory(category: Category)

    suspend fun deleteCategory(id: Long)

}
