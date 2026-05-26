package ru.zagrebin.front_mobile.ui.screens.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.data.AppContainer
import ru.zagrebin.front_mobile.data.remote.api.AuthRequest

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val api = AppContainer(application).feedApi
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

            runCatching {
                api.login(AuthRequest(email = current.loginOrEmail.trim(), password = current.password))
            }.onSuccess {
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false, error = "Неверный логин или пароль")
            }

        }
    }
}
