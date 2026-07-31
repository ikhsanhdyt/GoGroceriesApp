package com.diavolo.gogroceriesapp.core.domain.usecase

import com.diavolo.gogroceriesapp.core.domain.model.GroceryList
import com.diavolo.gogroceriesapp.core.domain.repository.GroceryListRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetListsUseCase @Inject constructor(
    private val repository: GroceryListRepository
) {
    operator fun invoke(): Flow<List<GroceryList>> {
        return repository.getAllLists()
    }
}