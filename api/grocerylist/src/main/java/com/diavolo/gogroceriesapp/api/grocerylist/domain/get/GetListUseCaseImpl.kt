package com.diavolo.gogroceriesapp.api.grocerylist.domain.get

import com.diavolo.gogroceriesapp.api.grocerylist.data.repository.GroceryListRepository
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryList
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetListUseCaseImpl @Inject constructor(
    private val repository: GroceryListRepository
) : GetListUseCase {

    override fun invoke(listId: Long): Flow<GroceryList?> =
        repository.observeListWithItems(listId)
}
