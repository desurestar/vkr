package ru.zagrebin.front_mobile.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.zagrebin.front_mobile.ui.screens.entryOptions.EntryOptionsScreen
import ru.zagrebin.front_mobile.ui.screens.profile.ProfileScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Recipes.route,
        modifier = Modifier
    ) {

        composable(BottomNavItem.Recipes.route) {
            EntryOptionsScreen(navController)
        }

        composable(BottomNavItem.Articles.route) {
            ProfileScreen()
        }

        composable(BottomNavItem.Statistics.route) {
            ProfileScreen()
        }

        composable(BottomNavItem.Profile.route) {
            ProfileScreen()
        }
    }
}