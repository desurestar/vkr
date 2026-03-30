package ru.zagrebin.front_mobile.ui.navigation

sealed class Screen(val route: String){
    data object Login: Screen("login")
    data object Register: Screen("register")

    data object EntryOptions: Screen("entryOptions")
}