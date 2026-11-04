package com.diavolo.gogroceriesapp.api.grocerylist.domain.calculation

import com.diavolo.gogroceriesapp.core.entity.common.Money
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryItem
import javax.inject.Inject
import kotlin.math.roundToLong

class ComputeEstimatedTotalUseCaseImpl @Inject constructor() : ComputeEstimatedTotalUseCase {

    override fun invoke(items: List<GroceryItem>): Money {
        val totalRupiah = items.sumOf { item ->
            val price = item.estimatedPriceRupiah ?: 0L
            (price * item.quantity).roundToLong()
        }
        return Money(totalRupiah)
    }
}
