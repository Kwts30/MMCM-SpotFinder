package com.cpe126L.mmcmspotfinder.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Room
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cpe126L.mmcmspotfinder.ui.components.PillBottomBar
import com.cpe126L.mmcmspotfinder.ui.components.PillTab
import com.cpe126L.mmcmspotfinder.ui.screens.ForecastScreen
import com.cpe126L.mmcmspotfinder.ui.screens.HomeScreen
import com.cpe126L.mmcmspotfinder.ui.screens.MapScreen
import com.cpe126L.mmcmspotfinder.ui.screens.MenuScreen
import com.cpe126L.mmcmspotfinder.ui.splash.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Map : Screen("map")
    object Forecast : Screen("forecast")
    object Menu : Screen("menu")
}

@Composable
@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
fun AppNav() {
    val navController = rememberNavController()

    val tabs = listOf(
        PillTab(Screen.Home.route, "Home", Icons.Outlined.Home),
        PillTab(Screen.Map.route, "Map", Icons.Outlined.Room),
        PillTab(Screen.Forecast.route, "Forecast", Icons.Outlined.QueryStats)
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            val backStack by navController.currentBackStackEntryAsState()
            val currentRoute = backStack?.destination?.route
            val menuActive = currentRoute == Screen.Menu.route

            if (currentRoute != Screen.Splash.route) {
                PillBottomBar(
                    currentRoute = currentRoute,
                    tabs = tabs,
                    menuActive = menuActive, // hides blue highlight when Menu is open
                    onSelectTab = { route ->
                        if (menuActive) navController.popBackStack() // close Menu first
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(Screen.Home.route) { saveState = true }
                        }
                    },
                    onCircleClick = {
                        if (menuActive) {
                            navController.popBackStack()
                        } else {
                            navController.navigate(Screen.Menu.route) { launchSingleTop = true }
                        }
                    }
                )
            }
        }
    ) { _ ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(
                route = Screen.Splash.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                SplashScreen {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            composable(
                route = Screen.Home.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) { HomeScreen() }

            composable(
                route = Screen.Map.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) { MapScreen() }

            composable(
                route = Screen.Forecast.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) { ForecastScreen() }

            composable(
                route = Screen.Menu.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) { MenuScreen() }
        }
    }
}