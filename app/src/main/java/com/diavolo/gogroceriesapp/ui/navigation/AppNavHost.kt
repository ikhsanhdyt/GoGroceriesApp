package com.diavolo.gogroceriesapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType.Companion.LongType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.diavolo.gogroceriesapp.feature.activeshopping.ActiveShoppingRoute
import com.diavolo.gogroceriesapp.feature.analytics.AnalyticsRoute
import com.diavolo.gogroceriesapp.feature.home.HomeRoute
import com.diavolo.gogroceriesapp.feature.listdetail.ListDetailRoute
import com.diavolo.gogroceriesapp.feature.tripsummary.TripSummaryRoute

private const val HOME_ROUTE = "home"
private const val LIST_ID_ARGUMENT = "listId"
private const val LIST_DETAIL_ROUTE = "list-detail/{$LIST_ID_ARGUMENT}"
private const val ACTIVE_SHOPPING_ROUTE = "active-shopping/{$LIST_ID_ARGUMENT}"
private const val TRIP_SUMMARY_ROUTE = "trip-summary/{$LIST_ID_ARGUMENT}"
private const val ANALYTICS_ROUTE = "analytics"

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE,
        modifier = modifier
    ) {
        composable(HOME_ROUTE) {
            HomeRoute(
                onListClick = { listId ->
                    navController.navigate("list-detail/$listId")
                },
                onAnalyticsClick = { navController.navigate(ANALYTICS_ROUTE) }
            )
        }

        composable(ANALYTICS_ROUTE) {
            AnalyticsRoute(
                onBackClick = navController::popBackStack,
                onTripClick = { listId ->
                    navController.navigate("trip-summary/$listId")
                }
            )
        }

        composable(
            route = LIST_DETAIL_ROUTE,
            arguments = listOf(
                navArgument(LIST_ID_ARGUMENT) { type = LongType }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getLong(LIST_ID_ARGUMENT)
                ?: return@composable
            ListDetailRoute(
                listId = listId,
                onBackClick = navController::popBackStack,
                onShoppingStarted = { startedListId ->
                    navController.navigate("active-shopping/$startedListId")
                },
                onTripSummaryClick = { completedListId ->
                    navController.navigate("trip-summary/$completedListId")
                }
            )
        }

        composable(
            route = ACTIVE_SHOPPING_ROUTE,
            arguments = listOf(
                navArgument(LIST_ID_ARGUMENT) { type = LongType }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getLong(LIST_ID_ARGUMENT)
                ?: return@composable
            ActiveShoppingRoute(
                listId = listId,
                onBackClick = navController::popBackStack,
                onShoppingFinished = {
                    navController.navigate("trip-summary/$listId") {
                        popUpTo(LIST_DETAIL_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = TRIP_SUMMARY_ROUTE,
            arguments = listOf(
                navArgument(LIST_ID_ARGUMENT) { type = LongType }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getLong(LIST_ID_ARGUMENT)
                ?: return@composable
            TripSummaryRoute(
                listId = listId,
                onBackClick = navController::popBackStack,
                onDoneClick = {
                    navController.popBackStack(HOME_ROUTE, inclusive = false)
                }
            )
        }
    }
}
