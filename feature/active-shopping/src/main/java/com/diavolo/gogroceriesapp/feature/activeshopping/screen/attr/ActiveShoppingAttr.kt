package com.diavolo.gogroceriesapp.feature.activeshopping.screen.attr

object ActiveShoppingAttr {
    internal const val TAG_FINISH_SHOPPING = "finish-shopping"

    internal fun tagShoppingItem(itemId: Long) = "shopping-item-$itemId"

    internal fun tagSetPrice(itemId: Long) = "set-price-$itemId"
}
