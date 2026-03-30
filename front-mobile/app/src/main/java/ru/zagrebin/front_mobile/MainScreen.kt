package ru.zagrebin.front_mobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.zagrebin.front_mobile.ui.components.BottomBar
import ru.zagrebin.front_mobile.ui.navigation.NavGraph

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(modifier = Modifier.fillMaxSize()) {

        // Контент
        NavGraph(
            navController = navController,
            paddingValues = androidx.compose.foundation.layout.PaddingValues()
        )

        // Меню ОВЕРЛЕЕМ снизу
        BottomBar(
            currentRoute = currentRoute,
            onTabClick = { item ->
                navController.navigate(item.route) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter) // <- вот это фиксит “вверху”
                .zIndex(1f)
        )
    }
}