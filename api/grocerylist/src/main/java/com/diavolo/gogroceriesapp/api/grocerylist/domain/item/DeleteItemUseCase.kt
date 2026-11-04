package com.diavolo.gogroceriesapp.api.grocerylist.domain.item

interface DeleteItemUseCase {
    suspend operator fun invoke(itemId: Long)
}
