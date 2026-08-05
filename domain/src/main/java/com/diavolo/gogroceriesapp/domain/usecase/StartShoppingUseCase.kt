package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.model.ListStatus
import com.diavolo.gogroceriesapp.domain.repository.GroceryListRepository
import javax.inject.Inject

class StartShoppingUseCase @Inject constructor(
    private val repository: GroceryListRepository
) {
    suspend operator fun invoke(list: GroceryList) {
        require(list.items.isNotEmpty()) { "A shopping list needs at least one item." }
        require(list.status == ListStatus.Draft || list.status == ListStatus.Active) {
            "Only draft or active lists can enter shopping mode."
        }
        if (list.status == ListStatus.Draft) {
            repository.update(list.copy(status = ListStatus.Active))
        }
    }
}
