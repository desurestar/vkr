package ru.zagrebin.front_mobile.ui.screens.publicProfile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.data.AppContainer
import ru.zagrebin.front_mobile.ui.screens.publicProfile.data.PublicProfileRepository
import ru.zagrebin.front_mobile.ui.screens.publicProfile.data.RemotePublicProfileRepository

class PublicProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val appContainer = AppContainer(application)
    private val repository: PublicProfileRepository = RemotePublicProfileRepository(appContainer.feedApi)
    private val _state = MutableStateFlow(PublicProfileUiState(isLoading = true))
    val state: StateFlow<PublicProfileUiState> = _state.asStateFlow()

    fun load(userId: String) {
        if (_state.value.userId == userId && _state.value.posts.isNotEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, userId = userId) }
            runCatching { repository.getPublicProfile(userId) }
                .onSuccess { profile ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            userId = profile.userId,
                            name = profile.name,
                            handle = profile.handle,
                            avatarUrl = profile.avatarUrl,
                            followingCount = formatFollowers(profile.followingCount),
                            followersCount = formatFollowers(profile.followersCount),
                            followingCountValue = profile.followingCount,
                            followersCountValue = profile.followersCount,
                            isFollowing = profile.isFollowing,
                            isOwnProfile = profile.isOwnProfile,
                            posts = profile.posts,
                            error = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(isLoading = false, error = throwable.message ?: "Не удалось загрузить профиль")
                    }
                }
        }
    }

    fun toggleLike(postId: Int) {
        val currentPost = _state.value.posts.firstOrNull { it.id == postId } ?: return
        val nextLiked = !currentPost.isLiked
        val optimisticPost = currentPost.copy(
            isLiked = nextLiked,
            likes = updateLikes(currentPost.likes, currentPost.isLiked, nextLiked)
        )
        _state.update { state ->
            state.copy(posts = state.posts.map { if (it.id == postId) optimisticPost else it })
        }

        viewModelScope.launch {
            val success = appContainer.feedRepository.toggleLike(postId, nextLiked)
            if (!success) {
                _state.update { state ->
                    state.copy(
                        posts = state.posts.map { if (it.id == postId) currentPost else it },
                        error = "Не удалось синхронизировать лайк с сервером."
                    )
                }
            }
        }
    }

    fun toggleFollow() {
        val current = _state.value
        if (current.isFollowUpdating || current.userId.isBlank() || current.isOwnProfile) return

        val targetFollowState = !current.isFollowing
        viewModelScope.launch {
            _state.update { it.copy(isFollowUpdating = true, isFollowing = targetFollowState) }
            runCatching { repository.setFollowState(current.userId, targetFollowState) }
                .onSuccess { updatedFollowState ->
                    _state.update {
                        val nextFollowersCount = recalculateFollowers(it.followersCountValue, updatedFollowState)
                        it.copy(
                            isFollowUpdating = false,
                            isFollowing = updatedFollowState,
                            followersCountValue = nextFollowersCount,
                            followersCount = formatFollowers(nextFollowersCount)
                        )
                    }
                }
                .onFailure {
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

    private fun updateLikes(likes: String, wasLiked: Boolean, nextLiked: Boolean): String {
        val currentLikes = likes.toIntOrNull() ?: 0
        val delta = when {
            nextLiked && !wasLiked -> 1
            !nextLiked && wasLiked -> -1
            else -> 0
        }
        return (currentLikes + delta).coerceAtLeast(0).toString()
    }

    private fun formatFollowers(value: Int): String =
        if (value >= 1_000) String.format(Locale.US, "%.1fk", value / 1_000f) else value.toString()

    private fun recalculateFollowers(currentValue: Int, becameFollowing: Boolean): Int =
        if (becameFollowing) currentValue + 1 else (currentValue - 1).coerceAtLeast(0)
}
