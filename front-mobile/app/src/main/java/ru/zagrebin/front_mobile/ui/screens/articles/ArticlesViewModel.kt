package ru.zagrebin.front_mobile.ui.screens.articles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.data.AppContainer
import ru.zagrebin.front_mobile.domain.model.FeedItem
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagState
import ru.zagrebin.front_mobile.ui.screens.feed.FeedState

private const val INITIAL_PAGE_SIZE = 10
private const val NEXT_PAGE_SIZE = 5

private data class LikeOverride(val isLiked: Boolean, val likes: String)
private data class PagingState(
    val isLoadingNextPage: Boolean = false,
    val hasMorePages: Boolean = true,
    val isUsingFallback: Boolean = false
)

class ArticlesViewModel(application: Application) : AndroidViewModel(application) {
    private val container = AppContainer(application)
    private val query = MutableStateFlow("")
    private val posts = MutableStateFlow<List<FeedItem>>(emptyList())
    private val errorMessage = MutableStateFlow<String?>(null)
    private val likeOverrides = MutableStateFlow<Map<Int, LikeOverride>>(emptyMap())
    private val pagingState = MutableStateFlow(PagingState())
    private var nextPage = 0
    private var searchJob: Job? = null

    val state: StateFlow<FeedState> = combine(
        posts,
        query,
        errorMessage,
        likeOverrides,
        pagingState
    ) { loadedPosts, q, error, overrides, paging ->
        FeedState(
            posts = loadedPosts.map { it.applyLikeOverride(overrides[it.id]).toUi() },
            searchQuery = q,
            errorMessage = error,
            isUsingFallback = paging.isUsingFallback,
            isLoadingNextPage = paging.isLoadingNextPage,
            hasMorePages = paging.hasMorePages
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeedState())

    init {
        retryRefresh()
    }

    fun retryRefresh() {
        loadFirstPage(query.value)
    }

    fun loadNextPage() {
        val paging = pagingState.value
        if (paging.isLoadingNextPage || !paging.hasMorePages || posts.value.isEmpty()) return
        viewModelScope.launch {
            pagingState.value = pagingState.value.copy(isLoadingNextPage = true)
            val result = container.feedRepository.loadArticlesPage(query.value, nextPage, NEXT_PAGE_SIZE)
            if (result.isFromCache) {
                errorMessage.value = "Сервер недоступен. Показан офлайн-кеш."
                pagingState.value = pagingState.value.copy(hasMorePages = false, isUsingFallback = true)
            } else {
                posts.value = (posts.value + result.data).distinctBy { it.id }
                nextPage += 1
                pagingState.value = pagingState.value.copy(hasMorePages = result.data.size >= NEXT_PAGE_SIZE)
                errorMessage.value = null
                pagingState.value = pagingState.value.copy(isUsingFallback = false)
            }
            pagingState.value = pagingState.value.copy(isLoadingNextPage = false)
        }
    }

    fun onSearch(newQuery: String) {
        query.value = newQuery
        loadFirstPage(newQuery)
    }

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

    private fun loadFirstPage(searchQuery: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            pagingState.value = PagingState()
            nextPage = 0
            val result = container.feedRepository.loadArticlesPage(searchQuery, nextPage, INITIAL_PAGE_SIZE)
            posts.value = result.data
            nextPage = 1
            pagingState.value = pagingState.value.copy(hasMorePages = !result.isFromCache && result.data.size >= INITIAL_PAGE_SIZE)
            if (result.isFromCache) {
                errorMessage.value = "Сервер недоступен. Показан офлайн-кеш."
                pagingState.value = pagingState.value.copy(isUsingFallback = true)
            } else {
                errorMessage.value = null
                pagingState.value = pagingState.value.copy(isUsingFallback = false)
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
