package com.diavolo.gogroceriesapp.core.ui.extentions

inline fun <T> T?.orDefault(
    default: T,
    additionalCheck: (T) -> Boolean = { _ -> true }
): T = if (this != null && additionalCheck(this)) this else default