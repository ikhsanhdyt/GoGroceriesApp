package com.diavolo.gogroceriesapp.api.category.data.mapper

import com.diavolo.gogroceriesapp.core.database.entity.CategoryEntity
import com.diavolo.gogroceriesapp.core.entity.category.Category

internal fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    colorHex = colorHex,
    aisleOrder = aisleOrder
)

internal fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    colorHex = colorHex,
    aisleOrder = aisleOrder
)
