package com.diavolo.gogroceriesapp.api.grocerylist.domain.calculation

import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryList
import com.diavolo.gogroceriesapp.core.entity.grocery.TripSummary

interface ComputeTripSummaryUseCase {
    operator fun invoke(list: GroceryList): TripSummary
}
