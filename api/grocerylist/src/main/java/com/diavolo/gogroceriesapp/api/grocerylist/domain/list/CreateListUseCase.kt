package com.diavolo.gogroceriesapp.api.grocerylist.domain.list

interface CreateListUseCase {
    suspend operator fun invoke(name: String, budgetRupiah: Long? = null): Long
}
