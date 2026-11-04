package com.diavolo.gogroceriesapp.api.category.data.local.service

import com.diavolo.gogroceriesapp.core.database.dao.CategoryDao
import com.diavolo.gogroceriesapp.core.database.entity.CategoryEntity
import javax.inject.Inject

class CategoryLocalDataSourceImpl @Inject constructor(
    private val dao: CategoryDao
) : CategoryLocalDataSource {

    override fun observeAll() = dao.observeAll()

    override suspend fun insert(category: CategoryEntity) = dao.insert(category)

    override suspend fun update(category: CategoryEntity) = dao.update(category)

    override suspend fun delete(id: Long) = dao.delete(id)
}
