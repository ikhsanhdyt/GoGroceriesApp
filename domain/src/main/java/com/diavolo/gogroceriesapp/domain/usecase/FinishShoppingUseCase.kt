package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.model.ListStatus
import com.diavolo.gogroceriesapp.domain.repository.GroceryListRepository
import javax.inject.Inject

class FinishShoppingUseCase @Inject constructor(
    private val repository: GroceryListRepository
) {
    suspend operator fun invoke(list: GroceryList) {
        require(list.status == ListStatus.Active) {
            "Only an active list can be completed."
        }
        repository.update(list.copy(status = ListStatus.Completed))
    }
}
