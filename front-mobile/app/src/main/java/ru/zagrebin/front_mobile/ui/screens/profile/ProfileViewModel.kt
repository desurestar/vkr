package ru.zagrebin.front_mobile.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.ui.screens.profile.components.ProfileEvent

class ProfileViewModel : ViewModel() {

    private val _state = MutableStateFlow(ProfileState(isLoading = true))
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            delay(600) // имитация загрузки
            _state.value = ProfileState(
                name = "Иван Иванов",
                email = "test@email.ru",
                avatarUrl = null,
                following = "828",
                followers = "72.9k",
                likes = "342.9k"
            )
        }
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.OnCreateClick -> println("Создать пост")
            ProfileEvent.OnMyPostsClick -> println("Мои посты")
            ProfileEvent.OnShoppingListClick -> println("Список покупок")
            ProfileEvent.OnDraftsClick -> println("Черновики")
            ProfileEvent.OnEditAccountClick -> println("Редактировать аккаунт")
            ProfileEvent.OnSecurityClick -> println("Безопасность")
        }
    }
}
