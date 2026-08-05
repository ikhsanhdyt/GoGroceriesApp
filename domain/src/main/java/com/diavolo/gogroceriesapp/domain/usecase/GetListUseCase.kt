package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.repository.GroceryListRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetListUseCase @Inject constructor(
    private val repository: GroceryListRepository
) {
    operator fun invoke(listId: Long): Flow<GroceryList?> =
        repository.observeListWithItems(listId)
}
