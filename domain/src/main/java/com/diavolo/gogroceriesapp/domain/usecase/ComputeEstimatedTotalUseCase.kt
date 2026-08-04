package com.diavolo.gogroceriesapp.domain.usecase

import com.diavolo.gogroceriesapp.domain.Money
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import kotlin.math.roundToLong
import javax.inject.Inject

class ComputeEstimatedTotalUseCase @Inject constructor() {
    operator fun invoke(items: List<GroceryItem>): Money {
        val totalRupiah = items.sumOf { item ->
            val price = item.estimatedPriceRupiah ?: 0L
            (price * item.quantity).roundToLong()
        }
        return Money(totalRupiah)
    }
}
