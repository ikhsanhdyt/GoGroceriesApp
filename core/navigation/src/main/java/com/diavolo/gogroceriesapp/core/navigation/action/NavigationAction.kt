package com.diavolo.gogroceriesapp.core.navigation.action

import com.diavolo.gogroceriesapp.core.navigation.base.BaseNavArgs
import kotlin.reflect.KClass

/** A navigation request from a screen. The app's nav host decides how to carry it out. */
sealed class NavigationAction {

    data object BackToPrevious : NavigationAction()

    data class NavigateTo(
        val route: BaseNavArgs,
        val popUpTo: KClass<out BaseNavArgs>? = null,
        val isInclusive: Boolean = false
    ) : NavigationAction()

    /** Pops back to the topmost [route] on the back stack; does nothing if it is not there. */
    data class BackToRoute(
        val route: KClass<out BaseNavArgs>,
        val inclusive: Boolean = false
    ) : NavigationAction()
}
