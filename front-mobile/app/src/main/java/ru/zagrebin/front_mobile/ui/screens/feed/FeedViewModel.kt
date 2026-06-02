package ru.zagrebin.front_mobile.ui.screens.feed

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
import ru.zagrebin.front_mobile.domain.model.FeedItem
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.data.RecipeRepository
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagState

class FeedViewModel(application: Application) : AndroidViewModel(application) {
    private val container = AppContainer(application)
    private val query = MutableStateFlow("")
    private val posts = MutableStateFlow<List<FeedItem>>(emptyList())
    private val errorMessage = MutableStateFlow<String?>(null)
    private val isUsingFallback = MutableStateFlow(false)

    val state: StateFlow<FeedState> = combine(
        posts,
        query,
        errorMessage,
        isUsingFallback
    ) { loadedPosts, q, error, fallback ->
        val mapped = loadedPosts.map { it.toUi() }.let { list ->
            if (q.isBlank()) list else list.filter { it.title.contains(q, true) }
        }
        FeedState(posts = mapped, searchQuery = q, errorMessage = error, isUsingFallback = fallback)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeedState())

    init {
        retryRefresh()
    }

    fun retryRefresh() {
        viewModelScope.launch {
            val result = container.feedRepository.loadRecipes()
            posts.value = result.data
            if (result.isFromCache) {
                errorMessage.value = "Сервер недоступен. Показан офлайн-кеш."
                isUsingFallback.value = true
            } else {
                errorMessage.value = null
                isUsingFallback.value = false
            }
        }
    }

    fun onSearch(newQuery: String) {
        query.value = newQuery
    }

    fun onTagClick(postId: Int, tagId: Int) = Unit

    fun onLikeClick(postId: Int) {
        val current = posts.value.firstOrNull { it.id == postId } ?: return
        val nextLiked = !current.isLiked
        posts.value = posts.value.map { item ->
            if (item.id == postId) item.withOptimisticLike(nextLiked) else item
        }
        viewModelScope.launch {
            val success = container.feedRepository.toggleLike(postId, nextLiked)
            if (!success) {
                posts.value = posts.value.map { item ->
                    if (item.id == postId) item.withOptimisticLike(!nextLiked) else item
                }
                errorMessage.value = "Не удалось синхронизировать лайк с сервером."
            }
        }
    }

    fun getPostById(postId: Int): PostCardState? = RecipeRepository.getPostById(postId)

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


private fun FeedItem.withOptimisticLike(nextLiked: Boolean): FeedItem {
    val currentLikes = likes.toIntOrNull() ?: 0
    val delta = when {
        nextLiked && !isLiked -> 1
        !nextLiked && isLiked -> -1
        else -> 0
    }
    return copy(isLiked = nextLiked, likes = (currentLikes + delta).coerceAtLeast(0).toString())
}
