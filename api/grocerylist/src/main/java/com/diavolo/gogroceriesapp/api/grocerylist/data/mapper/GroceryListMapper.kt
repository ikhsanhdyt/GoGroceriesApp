package com.diavolo.gogroceriesapp.api.grocerylist.data.mapper

import com.diavolo.gogroceriesapp.core.database.entity.GroceryItemEntity
import com.diavolo.gogroceriesapp.core.database.entity.GroceryListEntity
import com.diavolo.gogroceriesapp.core.database.entity.relations.ListWithItems
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryItem
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryList
import com.diavolo.gogroceriesapp.core.entity.grocery.ListStatus
import com.diavolo.gogroceriesapp.core.entity.grocery.UnitOfMeasure

internal fun GroceryItemEntity.toDomain(): GroceryItem = GroceryItem(
    id = id,
    listId = listId,
    categoryId = categoryId,
    name = name,
    quantity = quantity,
    unit = runCatching { UnitOfMeasure.valueOf(unit) }.getOrDefault(UnitOfMeasure.PIECE),
    estimatedPriceRupiah = estimatedPriceRupiah,
    actualPriceRupiah = actualPriceRupiah,
    isChecked = isChecked,
    notes = notes,
    position = position
)

internal fun GroceryItem.toEntity(): GroceryItemEntity = GroceryItemEntity(
    id = id,
    listId = listId,
    categoryId = categoryId,
    name = name,
    quantity = quantity,
    unit = unit.name,
    estimatedPriceRupiah = estimatedPriceRupiah,
    actualPriceRupiah = actualPriceRupiah,
    isChecked = isChecked,
    notes = notes,
    position = position
)

internal fun GroceryListEntity.toDomain(items: List<GroceryItem> = emptyList()): GroceryList =
    GroceryList(
        id = id,
        name = name,
        status = runCatching { ListStatus.valueOf(status) }.getOrDefault(ListStatus.Draft),
        budgetRupiah = budgetRupiah,
        createdAt = createdAt,
        updatedAt = updatedAt,
        items = items,
        completedAt = completedAt
    )

internal fun ListWithItems.toDomain(): GroceryList = list.toDomain(
    items = items.sortedBy { it.position }.map { it.toDomain() }
)
