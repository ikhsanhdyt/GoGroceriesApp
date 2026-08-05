package com.diavolo.gogroceriesapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.diavolo.gogroceriesapp.feature.home.HomeRoute
import com.diavolo.gogroceriesapp.feature.listdetail.ListDetailRoute

private const val HOME_ROUTE = "home"
private const val LIST_ID_ARGUMENT = "listId"
private const val LIST_DETAIL_ROUTE = "list-detail/{$LIST_ID_ARGUMENT}"

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
                }
            )
        }

        composable(
            route = LIST_DETAIL_ROUTE,
            arguments = listOf(
                navArgument(LIST_ID_ARGUMENT) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getLong(LIST_ID_ARGUMENT)
                ?: return@composable
            ListDetailRoute(
                listId = listId,
                onBackClick = navController::popBackStack
            )
        }
    }
}
