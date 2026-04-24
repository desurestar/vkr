package ru.zagrebin.front_mobile.ui.navigation

sealed class Screen(val route: String){
    data object Login: Screen("login")
    data object Register: Screen("register")

    data object EntryOptions: Screen("entryOptions")

    data object PublicProfile : Screen("publicProfile/{userId}") {
        fun createRoute(userId: String): String = "publicProfile/$userId"
    }

    data object RecipeDetails : Screen("recipe/{postId}") {
        fun createRoute(postId: Int): String = "recipe/$postId"
    }
}