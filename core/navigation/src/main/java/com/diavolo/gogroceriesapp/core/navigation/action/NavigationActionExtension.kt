package com.diavolo.gogroceriesapp.core.navigation.action

import com.diavolo.gogroceriesapp.core.navigation.action.NavigationAction.BackToPrevious
import com.diavolo.gogroceriesapp.core.navigation.action.NavigationAction.BackToRoute
import com.diavolo.gogroceriesapp.core.navigation.action.NavigationAction.NavigateTo
import com.diavolo.gogroceriesapp.core.navigation.base.BaseNavArgs
import kotlin.reflect.KClass

fun ((NavigationAction) -> Unit).navigateTo(
    route: BaseNavArgs,
    popUpTo: KClass<out BaseNavArgs>? = null,
    isInclusive: Boolean = false
) {
    this(NavigateTo(route, popUpTo, isInclusive))
}

fun ((NavigationAction) -> Unit).backToPrevious() {
    this(BackToPrevious)
}

inline fun <reified T : BaseNavArgs> ((NavigationAction) -> Unit).backTo(inclusive: Boolean = false) {
    this(BackToRoute(T::class, inclusive))
}
