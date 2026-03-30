package ru.zagbrebin.front_mobile.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FeatherIcons
import compose.icons.feathericons.BarChart2
import compose.icons.feathericons.Book
import compose.icons.feathericons.Columns
import compose.icons.feathericons.User

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Recipes : BottomNavItem(
        route = "recipes",
        title = "Рецепты",
        icon = FeatherIcons.Book
    )

    data object Articles : BottomNavItem(
        route = "articles",
        title = "Статьи",
        icon = FeatherIcons.Columns
    )

    data object Statistics : BottomNavItem(
        route = "statistics",
        title = "Статистика",
        icon = FeatherIcons.BarChart2
    )

    data object Profile : BottomNavItem(
        route = "profile",
        title = "Профиль",
        icon = FeatherIcons.User
    )
}