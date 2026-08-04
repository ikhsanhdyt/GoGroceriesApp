package com.diavolo.gogroceriesapp.data.mapper

import com.diavolo.gogroceriesapp.data.local.entity.CategoryEntity
import com.diavolo.gogroceriesapp.data.local.entity.GroceryItemEntity
import com.diavolo.gogroceriesapp.data.local.entity.GroceryListEntity
import com.diavolo.gogroceriesapp.data.local.entity.relations.ListWithItems
import com.diavolo.gogroceriesapp.domain.model.Category
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.model.ListStatus
import com.diavolo.gogroceriesapp.domain.model.UnitOfMeasure

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    colorHex = colorHex,
    aisleOrder = aisleOrder
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    colorHex = colorHex,
    aisleOrder = aisleOrder
)

fun GroceryItemEntity.toDomain(): GroceryItem = GroceryItem(
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

fun GroceryItem.toEntity(): GroceryItemEntity = GroceryItemEntity(
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

fun GroceryListEntity.toDomain(items: List<GroceryItem> = emptyList()): GroceryList = GroceryList(
    id = id,
    name = name,
    status = runCatching { ListStatus.valueOf(status) }.getOrDefault(ListStatus.Draft),
    budgetRupiah = budgetRupiah,
    createdAt = createdAt,
    updatedAt = updatedAt,
    items = items
)

fun ListWithItems.toDomain(): GroceryList = list.toDomain(
    items = items.sortedBy { it.position }.map { it.toDomain() }
)
