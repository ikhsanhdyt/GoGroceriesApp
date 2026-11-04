package com.diavolo.gogroceriesapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.diavolo.gogroceriesapp.core.navigation.action.NavigationAction
import com.diavolo.gogroceriesapp.core.navigation.base.BaseNavArgs
import com.diavolo.gogroceriesapp.core.navigation.navargs.activeshopping.ActiveShoppingNavArgs
import com.diavolo.gogroceriesapp.core.navigation.navargs.analytics.AnalyticsNavArgs
import com.diavolo.gogroceriesapp.core.navigation.navargs.categories.CategoriesNavArgs
import com.diavolo.gogroceriesapp.core.navigation.navargs.home.HomeNavArgs
import com.diavolo.gogroceriesapp.core.navigation.navargs.listdetail.ListDetailNavArgs
import com.diavolo.gogroceriesapp.core.navigation.navargs.tripsummary.TripSummaryNavArgs
import com.diavolo.gogroceriesapp.feature.activeshopping.screen.ActiveShoppingRoute
import com.diavolo.gogroceriesapp.feature.analytics.screen.AnalyticsRoute
import com.diavolo.gogroceriesapp.feature.categories.screen.CategoriesRoute
import com.diavolo.gogroceriesapp.feature.home.screen.HomeRoute
import com.diavolo.gogroceriesapp.feature.listdetail.screen.ListDetailRoute
import com.diavolo.gogroceriesapp.feature.tripsummary.screen.TripSummaryRoute
import kotlin.reflect.KClass

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navAction: (NavigationAction) -> Unit = navController::handle

    NavHost(
        navController = navController,
        startDestination = HomeNavArgs,
        modifier = modifier
    ) {
        composable<HomeNavArgs> { HomeRoute(navAction = navAction) }

        composable<CategoriesNavArgs> { CategoriesRoute(navAction = navAction) }

        composable<AnalyticsNavArgs> { AnalyticsRoute(navAction = navAction) }

        composable<ListDetailNavArgs> { backStackEntry ->
            ListDetailRoute(
                navArgs = backStackEntry.toRoute(),
                navAction = navAction
            )
        }

        composable<ActiveShoppingNavArgs> { backStackEntry ->
            ActiveShoppingRoute(
                navArgs = backStackEntry.toRoute(),
                navAction = navAction
            )
        }

        composable<TripSummaryNavArgs> { backStackEntry ->
            TripSummaryRoute(
                navArgs = backStackEntry.toRoute(),
                navAction = navAction
            )
        }
    }
}

private fun NavController.handle(action: NavigationAction) {
    when (action) {
        NavigationAction.BackToPrevious -> popBackStack()

        is NavigationAction.NavigateTo -> navigate(action.route) {
            action.popUpTo?.let(::destinationIdOf)?.let { destinationId ->
                popUpTo(destinationId) { inclusive = action.isInclusive }
            }
        }

        is NavigationAction.BackToRoute -> {
            destinationIdOf(action.route)?.let { destinationId ->
                popBackStack(destinationId, action.inclusive)
            }
        }
    }
}

// The KClass overloads of popUpTo and the back stack itself are restricted APIs, so routes are
// resolved to destination ids, which the public overloads accept.
private fun NavController.destinationIdOf(route: KClass<out BaseNavArgs>): Int? =
    graph.firstOrNull { it.hasRoute(route) }?.id
