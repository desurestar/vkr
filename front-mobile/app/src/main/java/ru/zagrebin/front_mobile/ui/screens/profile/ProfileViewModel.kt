package ru.zagrebin.front_mobile.ui.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.data.AppContainer
import ru.zagrebin.front_mobile.ui.screens.profile.components.ProfileEvent

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppContainer(application).let {
        ProfileRepository(it.feedApi, it.db.profileDao(), it.networkConnectionChecker)
    }
    private val profile = MutableStateFlow<ProfileData?>(null)
    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    val state: StateFlow<ProfileState> = combine(
        profile,
        isRefreshing,
        errorMessage
    ) { loadedProfile, refreshing, error ->
        loadedProfile?.toState(isLoading = false, error = error)
            ?: ProfileState(isLoading = refreshing, error = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileState(isLoading = true))

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            isRefreshing.value = true
            errorMessage.value = null
            runCatching { repository.getMyProfile() }
                .onSuccess { result ->
                    profile.value = result.profile
                    errorMessage.value = if (result.isFromCache) "Сервер недоступен. Показан офлайн-кеш." else null
                }
                .onFailure { error ->
                    errorMessage.value = error.message ?: "Не удалось загрузить профиль"
                }
            isRefreshing.value = false
        }
    }

    fun onEvent(event: ProfileEvent) { }
}

private fun ProfileData.toState(isLoading: Boolean, error: String?) = ProfileState(
    isLoading = isLoading,
    userId = id,
    name = name,
    email = email,
    bio = bio,
    avatarUrl = avatarUrl,
    following = followingCount.toString(),
    followers = followersCount.toString(),
    likes = "0",
    error = error
)
