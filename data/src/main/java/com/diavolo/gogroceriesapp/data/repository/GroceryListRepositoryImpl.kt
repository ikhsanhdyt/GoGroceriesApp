package com.diavolo.gogroceriesapp.data.repository

import com.diavolo.gogroceriesapp.data.local.dao.GroceryItemDao
import com.diavolo.gogroceriesapp.data.local.dao.GroceryListDao
import com.diavolo.gogroceriesapp.data.local.entity.GroceryListEntity
import com.diavolo.gogroceriesapp.data.mapper.toDomain
import com.diavolo.gogroceriesapp.data.mapper.toEntity
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.model.ListStatus
import com.diavolo.gogroceriesapp.domain.repository.GroceryListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GroceryListRepositoryImpl @Inject constructor(
    private val listDao: GroceryListDao,
    private val itemDao: GroceryItemDao
) : GroceryListRepository {

    override fun observeLists(): Flow<List<GroceryList>> {
        return listDao.observeAllWithItems().map { relations ->
            relations.map { it.toDomain() }
        }
    }

    override fun observeListWithItems(id: Long): Flow<GroceryList?> {
        return listDao.observeWithItems(id).map { relation ->
            relation?.toDomain()
        }
    }

    override suspend fun create(name: String, budgetRupiah: Long?): Long =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val entity = GroceryListEntity(
                name = name,
                status = ListStatus.Draft.name,
                budgetRupiah = budgetRupiah,
                createdAt = now,
                updatedAt = now
            )
            listDao.insert(entity)
        }

    override suspend fun update(list: GroceryList) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        // Fetch or reconstruct basic entity with updated timestamp
        val entity = GroceryListEntity(
            id = list.id,
            name = list.name,
            status = list.status.name,
            budgetRupiah = list.budgetRupiah,
            createdAt = list.createdAt,
            updatedAt = now
        )
        listDao.update(entity)
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        listDao.delete(id)
    }

    override suspend fun addItem(item: GroceryItem): Long = withContext(Dispatchers.IO) {
        itemDao.insert(item.toEntity())
    }

    override suspend fun updateItem(item: GroceryItem) = withContext(Dispatchers.IO) {
        itemDao.update(item.toEntity())
    }

    override suspend fun toggleChecked(itemId: Long, checked: Boolean) =
        withContext(Dispatchers.IO) {
            itemDao.setChecked(itemId, checked)
        }

    override suspend fun deleteItem(itemId: Long) = withContext(Dispatchers.IO) {
        itemDao.delete(itemId)
    }
}
