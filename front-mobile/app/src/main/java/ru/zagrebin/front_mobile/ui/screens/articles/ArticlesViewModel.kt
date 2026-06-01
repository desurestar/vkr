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
import ru.zagrebin.front_mobile.ui.screens.feed.FeedState

class ArticlesViewModel(application: Application) : AndroidViewModel(application) {
    private val container = AppContainer(application)
    private val query = MutableStateFlow("")
    private val errorMessage = MutableStateFlow<String?>(null)
    private val isUsingFallback = MutableStateFlow(false)

    val state: StateFlow<FeedState> = combine(
        container.observeArticlesFeedUseCase(),
        query,
        errorMessage,
        isUsingFallback
    ) { posts, q, error, fallback ->
        val mapped = posts.map { it.toUi() }.let { list ->
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
        time = time,
        calories = calories,
        views = views
    )
}