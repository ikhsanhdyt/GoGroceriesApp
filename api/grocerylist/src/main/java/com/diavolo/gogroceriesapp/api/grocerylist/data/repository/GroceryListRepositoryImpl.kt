package com.diavolo.gogroceriesapp.api.grocerylist.data.repository

import com.diavolo.gogroceriesapp.api.grocerylist.data.local.service.GroceryListLocalDataSource
import com.diavolo.gogroceriesapp.api.grocerylist.data.mapper.toDomain
import com.diavolo.gogroceriesapp.api.grocerylist.data.mapper.toEntity
import com.diavolo.gogroceriesapp.core.database.entity.GroceryListEntity
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryItem
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryList
import com.diavolo.gogroceriesapp.core.entity.grocery.ListStatus
import com.diavolo.gogroceriesapp.core.util.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GroceryListRepositoryImpl @Inject constructor(
    private val local: GroceryListLocalDataSource,
    private val timeProvider: TimeProvider
) : GroceryListRepository {

    override fun observeLists(): Flow<List<GroceryList>> {
        return local.observeAllWithItems().map { relations ->
            relations.map { it.toDomain() }
        }
    }

    override fun observeListWithItems(id: Long): Flow<GroceryList?> {
        return local.observeWithItems(id).map { relation ->
            relation?.toDomain()
        }
    }

    override suspend fun create(name: String, budgetRupiah: Long?): Long {
        val now = timeProvider.nowMillis()
        val entity = GroceryListEntity(
            name = name,
            status = ListStatus.Draft.name,
            budgetRupiah = budgetRupiah,
            createdAt = now,
            updatedAt = now
        )
        return local.insertList(entity)
    }

    override suspend fun update(list: GroceryList) {
        val entity = GroceryListEntity(
            id = list.id,
            name = list.name,
            status = list.status.name,
            budgetRupiah = list.budgetRupiah,
            createdAt = list.createdAt,
            updatedAt = timeProvider.nowMillis(),
            completedAt = list.completedAt
        )
        local.updateList(entity)
    }

    override suspend fun delete(id: Long) = local.deleteList(id)

    override suspend fun addItem(item: GroceryItem): Long = local.insertItem(item.toEntity())

    override suspend fun updateItem(item: GroceryItem) = local.updateItem(item.toEntity())

    override suspend fun toggleChecked(itemId: Long, checked: Boolean) =
        local.setItemChecked(itemId, checked)

    override suspend fun deleteItem(itemId: Long) = local.deleteItem(itemId)
}
