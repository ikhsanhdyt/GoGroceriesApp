package com.diavolo.gogroceriesapp.api.grocerylist.domain.shopping

import com.diavolo.gogroceriesapp.api.grocerylist.data.repository.GroceryListRepository
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryList
import com.diavolo.gogroceriesapp.core.entity.grocery.ListStatus
import javax.inject.Inject

class StartShoppingUseCaseImpl @Inject constructor(
    private val repository: GroceryListRepository
) : StartShoppingUseCase {

    override suspend fun invoke(list: GroceryList) {
        require(list.items.isNotEmpty()) { "A shopping list needs at least one item." }
        require(list.status == ListStatus.Draft || list.status == ListStatus.Active) {
            "Only draft or active lists can enter shopping mode."
        }
        if (list.status == ListStatus.Draft) {
            repository.update(list.copy(status = ListStatus.Active))
        }
    }
}
