package com.diavolo.gogroceriesapp.api.grocerylist.domain.get

import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryList
import kotlinx.coroutines.flow.Flow

interface GetListUseCase {
    operator fun invoke(listId: Long): Flow<GroceryList?>
}
