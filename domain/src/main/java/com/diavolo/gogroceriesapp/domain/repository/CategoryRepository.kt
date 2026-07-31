package com.diavolo.gogroceriesapp.domain.repository

import com.diavolo.gogroceriesapp.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun observeCategories(): Flow<List<Category>>

    suspend fun insertCategory(category: Category): Long

}