package com.diavolo.gogroceriesapp.api.grocerylist.domain.shopping

import com.diavolo.gogroceriesapp.api.grocerylist.data.repository.GroceryListRepository
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryList
import com.diavolo.gogroceriesapp.core.entity.grocery.ListStatus
import com.diavolo.gogroceriesapp.core.util.TimeProvider
import javax.inject.Inject

class FinishShoppingUseCaseImpl @Inject constructor(
    private val repository: GroceryListRepository,
    private val timeProvider: TimeProvider
) : FinishShoppingUseCase {

    override suspend fun invoke(list: GroceryList) {
        require(list.status == ListStatus.Active) {
            "Only an active list can be completed."
        }
        repository.update(
            list.copy(
                status = ListStatus.Completed,
                completedAt = timeProvider.nowMillis()
            )
        )
    }
}
