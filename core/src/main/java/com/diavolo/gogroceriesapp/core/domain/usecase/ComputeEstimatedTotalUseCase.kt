package com.diavolo.gogroceriesapp.core.domain.usecase

import com.diavolo.gogroceriesapp.core.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.core.domain.model.Money
import javax.inject.Inject

class ComputeEstimatedTotalUseCase @Inject constructor() {
    operator fun invoke(items: List<GroceryItem>): Money {
        return items.fold(Money.zero()) { acc, item ->
            val itemTotal = item.estimatedPrice?.amount ?: 0L
            acc + Money(itemTotal)
        }
    }
}