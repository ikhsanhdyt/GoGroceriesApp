package com.diavolo.gogroceriesapp.api.grocerylist.data.local.service

import com.diavolo.gogroceriesapp.core.database.dao.GroceryItemDao
import com.diavolo.gogroceriesapp.core.database.dao.GroceryListDao
import com.diavolo.gogroceriesapp.core.database.entity.GroceryItemEntity
import com.diavolo.gogroceriesapp.core.database.entity.GroceryListEntity
import javax.inject.Inject

class GroceryListLocalDataSourceImpl @Inject constructor(
    private val listDao: GroceryListDao,
    private val itemDao: GroceryItemDao
) : GroceryListLocalDataSource {

    override fun observeAllWithItems() = listDao.observeAllWithItems()

    override fun observeWithItems(id: Long) = listDao.observeWithItems(id)

    override suspend fun insertList(list: GroceryListEntity) = listDao.insert(list)

    override suspend fun updateList(list: GroceryListEntity) = listDao.update(list)

    override suspend fun deleteList(id: Long) = listDao.delete(id)

    override suspend fun insertItem(item: GroceryItemEntity) = itemDao.insert(item)

    override suspend fun updateItem(item: GroceryItemEntity) = itemDao.update(item)

    override suspend fun setItemChecked(id: Long, checked: Boolean) = itemDao.setChecked(id, checked)

    override suspend fun deleteItem(id: Long) = itemDao.delete(id)
}
