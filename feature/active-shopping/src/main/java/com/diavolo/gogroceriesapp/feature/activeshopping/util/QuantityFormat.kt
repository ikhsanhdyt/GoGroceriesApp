package com.diavolo.gogroceriesapp.feature.activeshopping.util

internal fun formatQuantity(quantity: Double): String =
    if (quantity % 1.0 == 0.0) quantity.toLong().toString() else quantity.toString()
