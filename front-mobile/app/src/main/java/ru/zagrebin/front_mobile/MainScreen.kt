package ru.zagrebin.front_mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.zagrebin.front_mobile.ui.components.BottomBar
import ru.zagrebin.front_mobile.ui.navigation.NavGraph
import ru.zagrebin.front_mobile.ui.navigation.BottomNavItem
import ru.zagrebin.front_mobile.ui.navigation.AuthSessionState
import ru.zagrebin.front_mobile.ui.navigation.Screen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val density = LocalDensity.current
    val topInset = with(density) { WindowInsets.statusBars.getTop(density).toDp() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val bottomRoutes = remember {
        setOf(
            BottomNavItem.Recipes.route,
            BottomNavItem.Articles.route,
            BottomNavItem.Statistics.route,
            BottomNavItem.Profile.route
        )
    }
    var selectedBottomRoute by remember { mutableStateOf(BottomNavItem.Recipes.route) }
    val isAuthorized by AuthSessionState.isAuthorized.collectAsState()

    LaunchedEffect(currentRoute) {
        if (currentRoute in bottomRoutes) {
            selectedBottomRoute = currentRoute ?: BottomNavItem.Recipes.route
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        NavGraph(
            navController = navController,
            paddingValues = PaddingValues(top = topInset)
        )

        TopFadeBlurOverlay(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(2f)
        ) {
            BottomBar(
                currentRoute = selectedBottomRoute,
                onTabClick = { item ->
                    if (item.route == BottomNavItem.Profile.route && !isAuthorized) {
                        navController.navigate(Screen.EntryOptions.route)
                        return@BottomBar
                    }
                    val isReselect = item.route == selectedBottomRoute
                    selectedBottomRoute = item.route

                    if (isReselect) {
                        // Повторный тап по вкладке: очищаем ее стек и пересоздаем экран.
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = false
                            popUpTo(item.route) {
                                inclusive = true
                                saveState = false
                            }
                        }
                    } else {
                        // Переход на другую вкладку: закрываем открытые вложенные страницы.
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun TopFadeBlurOverlay(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to Color.Transparent,
                        0.2f to Color.Black.copy(alpha = 0.05f),
                        0.45f to Color.Black.copy(alpha = 0.28f),
                        0.75f to Color.Black.copy(alpha = 0.54f),
                        1f to Color.Black.copy(alpha = 0.82f)
                    )
                )
            )
    )
}
