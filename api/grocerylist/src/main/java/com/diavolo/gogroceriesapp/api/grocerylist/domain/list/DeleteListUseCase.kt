package com.diavolo.gogroceriesapp.api.grocerylist.domain.list

interface DeleteListUseCase {
    suspend operator fun invoke(id: Long)
}
