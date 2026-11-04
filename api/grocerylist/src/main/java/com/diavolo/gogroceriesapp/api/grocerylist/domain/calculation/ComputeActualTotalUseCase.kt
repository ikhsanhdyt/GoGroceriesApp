package com.diavolo.gogroceriesapp.api.grocerylist.domain.calculation

import com.diavolo.gogroceriesapp.core.entity.common.Money
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryItem

interface ComputeActualTotalUseCase {
    operator fun invoke(items: List<GroceryItem>): Money
}
