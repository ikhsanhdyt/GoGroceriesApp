package com.diavolo.gogroceriesapp.data.local

import com.diavolo.gogroceriesapp.data.local.dao.CategoryDao
import com.diavolo.gogroceriesapp.data.local.entity.CategoryEntity
import javax.inject.Inject

class DefaultCategorySeeder @Inject constructor(
    private val categoryDao: CategoryDao
) {
    suspend fun seed() {
        val existingNames = categoryDao.getNames()
            .map { it.trim().lowercase() }
            .toSet()
        val missingCategories = DEFAULT_CATEGORIES.filterNot { category ->
            category.name.lowercase() in existingNames
        }

        if (missingCategories.isNotEmpty()) {
            categoryDao.insertAll(missingCategories)
        }
    }
}

internal val DEFAULT_CATEGORIES = listOf(
    CategoryEntity(
        name = "Produce",
        colorHex = "#4F8A5B",
        aisleOrder = 0
    ),
    CategoryEntity(
        name = "Dairy & Chilled",
        colorHex = "#4A90A4",
        aisleOrder = 1
    ),
    CategoryEntity(
        name = "Meat & Seafood",
        colorHex = "#C75B5B",
        aisleOrder = 2
    ),
    CategoryEntity(
        name = "Bakery",
        colorHex = "#C98B4A",
        aisleOrder = 3
    ),
    CategoryEntity(
        name = "Frozen",
        colorHex = "#5B7DB1",
        aisleOrder = 4
    ),
    CategoryEntity(
        name = "Pantry",
        colorHex = "#A77A3D",
        aisleOrder = 5
    ),
    CategoryEntity(
        name = "Household",
        colorHex = "#7A6F9B",
        aisleOrder = 6
    )
)
