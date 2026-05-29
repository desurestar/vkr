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
import ru.zagrebin.front_mobile.data.repository.RefreshResult
import ru.zagrebin.front_mobile.ui.screens.profile.components.ProfileEvent

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppContainer(application).let {
        ProfileRepository(it.feedApi, it.db.profileDao(), it.networkConnectionChecker)
    }
    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    val state: StateFlow<ProfileState> = combine(
        repository.observeMyProfile(),
        isRefreshing,
        errorMessage
    ) { profile, refreshing, error ->
        profile?.toState(isLoading = false, error = error)
            ?: ProfileState(isLoading = refreshing, error = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileState(isLoading = true))

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            isRefreshing.value = true
            errorMessage.value = null
            val result = repository.refreshMyProfile()
            if (result == RefreshResult.Fallback) {
                errorMessage.value = "Сервер недоступен. Показан офлайн-кеш."
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
