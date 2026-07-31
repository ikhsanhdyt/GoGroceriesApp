package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.Money
import javax.inject.Inject

class ComputeEstimatedTotalUseCase @Inject constructor() {
    operator fun invoke(items: List<GroceryItem>): Long {
        return items.sumOf { item ->
            val price = item.estimatedPriceCents ?: 0L
            (price * item.quantity).toLong()
        }
    }
}