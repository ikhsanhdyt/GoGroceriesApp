package com.diavolo.gogroceriesapp.api.category.data.local.service

import com.diavolo.gogroceriesapp.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryLocalDataSource {
    fun observeAll(): Flow<List<CategoryEntity>>

    suspend fun insert(category: CategoryEntity): Long

    suspend fun update(category: CategoryEntity)

    suspend fun delete(id: Long)
}
