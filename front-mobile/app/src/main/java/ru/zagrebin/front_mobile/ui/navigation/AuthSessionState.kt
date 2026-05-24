package ru.zagrebin.front_mobile.ui.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AuthSessionState {
    private val _isAuthorized = MutableStateFlow(false)
    val isAuthorized: StateFlow<Boolean> = _isAuthorized

    fun setAuthorized(value: Boolean) {
        _isAuthorized.value = value
    }
}
