package ru.zagrebin.front_mobile.ui.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.data.AppContainer
import ru.zagrebin.front_mobile.ui.screens.profile.components.ProfileEvent

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppContainer(application).let {
        ProfileRepository(it.feedApi, it.db.profileDao())
    }

    private val _state = MutableStateFlow(ProfileState(isLoading = true))
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching { repository.getMyProfile() }
                .onSuccess { p ->
                    _state.value = ProfileState(
                        isLoading = false,
                        userId = p.id,
                        name = p.name,
                        email = p.email,
                        bio = p.bio,
                        avatarUrl = p.avatarUrl,
                        following = p.followingCount.toString(),
                        followers = p.followersCount.toString(),
                        likes = "0"
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(isLoading = false, error = it.message)
                }
        }
    }

    fun onEvent(event: ProfileEvent) { }
}
