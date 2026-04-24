package ru.zagrebin.front_mobile.ui.screens.publicProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import ru.zagrebin.front_mobile.ui.screens.publicProfile.data.FakePublicProfileRepository
import ru.zagrebin.front_mobile.ui.screens.publicProfile.data.PublicProfileRepository

class PublicProfileViewModel(
    private val repository: PublicProfileRepository = FakePublicProfileRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(PublicProfileUiState(isLoading = true))
    val state: StateFlow<PublicProfileUiState> = _state.asStateFlow()

    fun load(userId: String) {
        if (_state.value.userId == userId && _state.value.posts.isNotEmpty()) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    userId = userId
                )
            }

            runCatching { repository.getPublicProfile(userId) }
                .onSuccess { profile ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            userId = profile.userId,
                            name = profile.name,
                            email = profile.email,
                            avatarUrl = profile.avatarUrl,
                            followingCount = profile.followingCount.toString(),
                            followersCount = formatFollowers(profile.followersCount),
                            isFollowing = profile.isFollowing,
                            posts = profile.posts,
                            error = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Не удалось загрузить профиль"
                        )
                    }
                }
        }
    }

    fun toggleFollow() {
        val current = _state.value
        if (current.isFollowUpdating || current.userId.isBlank()) return

        val targetFollowState = !current.isFollowing

        viewModelScope.launch {
            _state.update { it.copy(isFollowUpdating = true, isFollowing = targetFollowState) }

            runCatching {
                repository.setFollowState(current.userId, targetFollowState)
            }.onSuccess { updatedFollowState ->
                _state.update {
                    it.copy(
                        isFollowUpdating = false,
                        isFollowing = updatedFollowState,
                        followersCount = recalculateFollowers(
                            currentValue = it.followersCount,
                            becameFollowing = updatedFollowState
                        )
                    )
                }
            }.onFailure {
                _state.update {
                    it.copy(
                        isFollowUpdating = false,
                        isFollowing = !targetFollowState,
                        error = "Не удалось обновить подписку"
                    )
                }
            }
        }
    }

    private fun formatFollowers(value: Int): String {
        return if (value >= 1_000) {
            String.format(Locale.US, "%.1fk", value / 1_000f)
        } else {
            value.toString()
        }
    }

    private fun recalculateFollowers(currentValue: String, becameFollowing: Boolean): String {
        val normalized = currentValue.lowercase().replace("k", "")
        val base = normalized.toFloatOrNull() ?: return currentValue

        val step = if (currentValue.lowercase().contains("k")) 0.1f else 1f
        val updated = if (becameFollowing) base + step else (base - step).coerceAtLeast(0f)

        return if (currentValue.lowercase().contains("k")) {
            String.format(Locale.US, "%.1fk", updated)
        } else {
            updated.toInt().toString()
        }
    }
}

