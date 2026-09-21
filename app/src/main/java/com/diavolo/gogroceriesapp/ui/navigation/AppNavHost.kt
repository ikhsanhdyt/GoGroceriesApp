package com.diavolo.gogroceriesapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.diavolo.gogroceriesapp.feature.activeshopping.ActiveShoppingRoute
import com.diavolo.gogroceriesapp.feature.analytics.AnalyticsRoute
import com.diavolo.gogroceriesapp.feature.home.HomeRoute
import com.diavolo.gogroceriesapp.feature.listdetail.ListDetailRoute
import com.diavolo.gogroceriesapp.feature.tripsummary.TripSummaryRoute

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Home,
        modifier = modifier
    ) {
        composable<Home> {
            HomeRoute(
                onListClick = { listId ->
                    navController.navigate(ListDetail(listId))
                },
                onAnalyticsClick = { navController.navigate(Analytics) }
            )
        }

        composable<Analytics> {
            AnalyticsRoute(
                onBackClick = navController::popBackStack,
                onTripClick = { listId ->
                    navController.navigate(TripSummary(listId))
                }
            )
        }

        composable<ListDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<ListDetail>()
            ListDetailRoute(
                listId = route.listId,
                onBackClick = navController::popBackStack,
                onShoppingStarted = { startedListId ->
                    navController.navigate(ActiveShopping(startedListId))
                },
                onTripSummaryClick = { completedListId ->
                    navController.navigate(TripSummary(completedListId))
                }
            )
        }

        composable<ActiveShopping> { backStackEntry ->
            val route = backStackEntry.toRoute<ActiveShopping>()
            ActiveShoppingRoute(
                listId = route.listId,
                onBackClick = navController::popBackStack,
                onShoppingFinished = {
                    navController.navigate(TripSummary(route.listId)) {
                        popUpTo<ListDetail> { inclusive = true }
                    }
                }
            )
        }

        composable<TripSummary> { backStackEntry ->
            val route = backStackEntry.toRoute<TripSummary>()
            TripSummaryRoute(
                listId = route.listId,
                onBackClick = navController::popBackStack,
                onDoneClick = {
                    navController.popBackStack<Home>(inclusive = false)
                }
            )
        }
    }
}
