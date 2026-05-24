package ru.zagrebin.front_mobile.ui.navigation

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AuthSessionState {
    private const val PREFS_NAME = "auth_session"
    private const val KEY_AUTHORIZED = "is_authorized"

    private val _isAuthorized = MutableStateFlow(false)
    val isAuthorized: StateFlow<Boolean> = _isAuthorized

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isAuthorized.value = prefs.getBoolean(KEY_AUTHORIZED, false)
    }

    fun setAuthorized(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_AUTHORIZED, value)
            .apply()
        _isAuthorized.value = value
    }

    fun setAuthorized(value: Boolean) {
        _isAuthorized.value = value
    }
}
