package ru.zagrebin.front_mobile.ui.screens.publicProfile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import ru.zagrebin.front_mobile.data.AppContainer
import ru.zagrebin.front_mobile.ui.screens.publicProfile.data.PublicProfileRepository
import ru.zagrebin.front_mobile.ui.screens.publicProfile.data.RemotePublicProfileRepository

class PublicProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PublicProfileRepository = RemotePublicProfileRepository(AppContainer(application).feedApi)
    private val _state = MutableStateFlow(PublicProfileUiState(isLoading = true))
    val state: StateFlow<PublicProfileUiState> = _state.asStateFlow()

    fun load(userId: String) { /* same */
        if (_state.value.userId == userId && _state.value.posts.isNotEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, userId = userId) }
            runCatching { repository.getPublicProfile(userId) }
                .onSuccess { profile ->
                    _state.update {
                        it.copy(isLoading = false, userId = profile.userId, name = profile.name, email = profile.email, avatarUrl = profile.avatarUrl, followingCount = profile.followingCount.toString(), followersCount = formatFollowers(profile.followersCount), isFollowing = profile.isFollowing, posts = profile.posts, error = null)
                    }
                }
                .onFailure { t -> _state.update { it.copy(isLoading = false, error = t.message ?: "Не удалось загрузить профиль") } }
        }
    }

    fun toggleFollow() { /* same */
        val current = _state.value
        if (current.isFollowUpdating || current.userId.isBlank()) return
        val targetFollowState = !current.isFollowing
        viewModelScope.launch {
            _state.update { it.copy(isFollowUpdating = true, isFollowing = targetFollowState) }
            runCatching { repository.setFollowState(current.userId, targetFollowState) }
                .onSuccess { updatedFollowState ->
                    _state.update { it.copy(isFollowUpdating = false, isFollowing = updatedFollowState, followersCount = recalculateFollowers(it.followersCount, updatedFollowState)) }
                }
                .onFailure { _state.update { it.copy(isFollowUpdating = false, isFollowing = !targetFollowState, error = "Не удалось обновить подписку") } }
        }
    }
    private fun formatFollowers(value: Int): String = if (value >= 1_000) String.format(Locale.US, "%.1fk", value / 1_000f) else value.toString()
    private fun recalculateFollowers(currentValue: String, becameFollowing: Boolean): String { val n = currentValue.lowercase().replace("k", "").toFloatOrNull() ?: return currentValue; val step = if (currentValue.lowercase().contains("k")) 0.1f else 1f; val u = if (becameFollowing) n+step else (n-step).coerceAtLeast(0f); return if (currentValue.lowercase().contains("k")) String.format(Locale.US,"%.1fk",u) else u.toInt().toString() }
}
