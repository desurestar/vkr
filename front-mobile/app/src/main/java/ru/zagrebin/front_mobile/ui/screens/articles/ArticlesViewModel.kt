package ru.zagrebin.front_mobile.ui.screens.articles

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
import ru.zagrebin.front_mobile.domain.model.FeedItem
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagState
import ru.zagrebin.front_mobile.ui.screens.feed.FeedState

private data class LikeOverride(val isLiked: Boolean, val likes: String)

class ArticlesViewModel(application: Application) : AndroidViewModel(application) {
    private val container = AppContainer(application)
    private val query = MutableStateFlow("")
    private val errorMessage = MutableStateFlow<String?>(null)
    private val isUsingFallback = MutableStateFlow(false)
    private val likeOverrides = MutableStateFlow<Map<Int, LikeOverride>>(emptyMap())

    val state: StateFlow<FeedState> = combine(
        container.observeArticlesFeedUseCase(),
        query,
        errorMessage,
        isUsingFallback,
        likeOverrides
    ) { posts, q, error, fallback, overrides ->
        val mapped = posts.map { it.applyLikeOverride(overrides[it.id]).toUi() }.let { list ->
            if (q.isBlank()) list else list.filter { it.title.contains(q, true) }
        }
        FeedState(posts = mapped, searchQuery = q, errorMessage = error, isUsingFallback = fallback)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeedState())

    init {
        retryRefresh()
    }

    fun retryRefresh() {
        viewModelScope.launch {
            val result = container.refreshArticlesFeedUseCase()
            when (result) {
                RefreshResult.Success -> {
                    errorMessage.value = null
                    isUsingFallback.value = false
                }
                RefreshResult.Fallback -> {
                    errorMessage.value = "Сервер недоступен. Показан офлайн-кеш."
                    isUsingFallback.value = true
                }
            }
        }
    }

    fun onSearch(newQuery: String) { query.value = newQuery }
    fun onTagClick(postId: Int, tagId: Int) = Unit

    fun onLikeClick(postId: Int) {
        val current = state.value.posts.firstOrNull { it.id == postId } ?: return
        val nextLiked = !current.isLiked
        val nextLikes = updateLikes(current.likes, current.isLiked, nextLiked)
        likeOverrides.value = likeOverrides.value + (postId to LikeOverride(nextLiked, nextLikes))
        viewModelScope.launch {
            val success = container.feedRepository.toggleLike(postId, nextLiked)
            if (!success) {
                likeOverrides.value = likeOverrides.value + (postId to LikeOverride(current.isLiked, current.likes))
                errorMessage.value = "Не удалось синхронизировать лайк с сервером."
            }
        }
    }

    private fun FeedItem.toUi(): PostCardState = PostCardState(
        id = id,
        authorId = authorId,
        authorName = authorName,
        authorHandle = authorHandle,
        authorAvatarUrl = authorAvatarUrl,
        date = date,
        title = title,
        imageUrl = imageUrl,
        likes = likes,
        isLiked = isLiked,
        time = time,
        calories = calories,
        views = views,
        tags = tags.map { TagState(it.id, it.name) }
    )
}

private fun FeedItem.applyLikeOverride(override: LikeOverride?): FeedItem =
    if (override == null) this else copy(isLiked = override.isLiked, likes = override.likes)

private fun updateLikes(likes: String, wasLiked: Boolean, nextLiked: Boolean): String {
    val currentLikes = likes.toIntOrNull() ?: 0
    val delta = when {
        nextLiked && !wasLiked -> 1
        !nextLiked && wasLiked -> -1
        else -> 0
    }
    return (currentLikes + delta).coerceAtLeast(0).toString()
}
