package ru.zagrebin.front_mobile.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    fun onLoginOrEmailChange(value: String) {
        _state.value = _state.value.copy(loginOrEmail = value)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value)
    }

    fun login() {
        val current = _state.value

        if(current.loginOrEmail.isBlank() || current.password.isBlank()) {
            _state.value = current.copy(error = "Заполните все поля")
            return
        }

        viewModelScope.launch {
            _state.value = current.copy(isLoading = true, error = null)

            delay(1500)

            val success = current.loginOrEmail == "test" && current.password == "123"

            if(success) {
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            } else {
                _state.value = _state.value.copy(isLoading = false, error = "Неверный логин или пароль")
            }

        }
    }
}