package ru.zagrebin.front_mobile.ui.screens.login

data class LoginState(
    val loginOrEmail: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)