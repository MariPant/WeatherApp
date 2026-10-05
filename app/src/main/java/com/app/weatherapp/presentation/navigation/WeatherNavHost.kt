package com.app.weatherapp.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.weatherapp.presentation.home.HomeRoute
import com.app.weatherapp.presentation.locations.LocationsRoute
import com.app.weatherapp.presentation.search.SearchRoute
import kotlinx.serialization.Serializable

@Serializable
data object HomeDestination

@Serializable
data object LocationsDestination

@Serializable
data object SearchDestination

private const val TRANSITION_MILLIS = 350

/** Type-safe Navigation Compose graph: Home -> My Locations -> Search. */
@Composable
fun WeatherNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backToHome: () -> Unit = { navController.popBackStack(HomeDestination, inclusive = false) }

    NavHost(
        navController = navController,
        startDestination = HomeDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(TRANSITION_MILLIS)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(TRANSITION_MILLIS),
            )
        },
        exitTransition = { fadeOut(tween(TRANSITION_MILLIS)) },
        popEnterTransition = { fadeIn(tween(TRANSITION_MILLIS)) },
        popExitTransition = {
            fadeOut(tween(TRANSITION_MILLIS)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(TRANSITION_MILLIS),
            )
        },
    ) {
        composable<HomeDestination> {
            HomeRoute(
                onOpenSearch = { navController.navigate(SearchDestination) { launchSingleTop = true } },
                onOpenLocations = { navController.navigate(LocationsDestination) { launchSingleTop = true } },
            )
        }
        composable<LocationsDestination> {
            LocationsRoute(
                onBack = { navController.popBackStack() },
                onOpenSearch = { navController.navigate(SearchDestination) { launchSingleTop = true } },
                onNavigateHome = backToHome,
            )
        }
        composable<SearchDestination> {
            SearchRoute(
                onBack = { navController.popBackStack() },
                onLocationChosen = backToHome,
            )
        }
    }
}
