package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.model.Category

class DuplicateCategoryNameException(name: String) :
    IllegalArgumentException("A category named \"$name\" already exists.")

/**
 * Returns [name] trimmed, or throws if it is blank or matches (ignoring case) one of [others].
 */
internal fun validateCategoryName(name: String, others: List<Category>): String {
    val trimmedName = name.trim()
    require(trimmedName.isNotEmpty()) { "Category name must not be blank." }
    if (others.any { it.name.trim().equals(trimmedName, ignoreCase = true) }) {
        throw DuplicateCategoryNameException(trimmedName)
    }
    return trimmedName
}
