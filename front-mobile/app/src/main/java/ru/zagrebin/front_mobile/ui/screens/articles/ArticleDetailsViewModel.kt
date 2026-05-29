package ru.zagrebin.front_mobile.ui.screens.articles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.data.AppContainer
import ru.zagrebin.front_mobile.data.repository.RefreshResult
import ru.zagrebin.front_mobile.domain.model.ArticleDetails
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState

data class ArticleDetailsUiState(
    val isLoading: Boolean = false,
    val post: PostCardState? = null,
    val content: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val container = AppContainer(application)
    private val currentId = MutableStateFlow<Int?>(null)
    private val isRefreshing = MutableStateFlow(false)
    private val hasLoadError = MutableStateFlow(false)

    val state: StateFlow<ArticleDetailsUiState> = currentId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else container.observeArticleDetailsUseCase(id)
        }
        .combine(isRefreshing) { details, refreshing -> details to refreshing }
        .combine(hasLoadError) { (details, refreshing), loadError ->
            val hasRequest = currentId.value != null
            ArticleDetailsUiState(
                isLoading = hasRequest && details == null && refreshing && !loadError,
                post = details?.toUi(),
                content = details?.content.orEmpty()
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ArticleDetailsUiState())

    fun load(postId: Int) {
        if (currentId.value == postId) return
        currentId.value = postId
        isRefreshing.value = true
        hasLoadError.value = false
        viewModelScope.launch {
            val result = runCatching { container.refreshArticleDetailsUseCase(postId) }
                .getOrDefault(RefreshResult.Fallback)
            hasLoadError.value = result == RefreshResult.Fallback
            isRefreshing.value = false
        }
    }

    private fun ArticleDetails.toUi(): PostCardState = PostCardState(
        id = id,
        authorId = authorId,
        authorName = authorName,
        authorHandle = authorHandle,
        date = date,
        title = title,
        imageUrl = imageUrl,
        likes = likes,
        time = "",
        calories = "",
        views = views,
        isSaved = isSaved
    )
}
