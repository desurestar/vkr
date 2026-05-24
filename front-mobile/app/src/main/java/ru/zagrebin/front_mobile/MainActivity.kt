package ru.zagrebin.front_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.zagrebin.front_mobile.ui.navigation.AuthSessionState
import ru.zagrebin.front_mobile.ui.navigation.Screen
import ru.zagrebin.front_mobile.ui.screens.entryOptions.EntryOptionsScreen
import ru.zagrebin.front_mobile.ui.screens.login.LoginScreen
import ru.zagrebin.front_mobile.ui.screens.register.RegisterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        AuthSessionState.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            MainScreen()
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {

    NavHost(
        navController = navController,
        startDestination = Screen.EntryOptions.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }

        composable(Screen.EntryOptions.route) {
            EntryOptionsScreen(navController)
        }
    }
}
