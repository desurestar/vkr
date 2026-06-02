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
import ru.zagrebin.front_mobile.domain.model.FeedItem
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagState

private const val RECIPE_TYPE = "RECIPE"
private const val ARTICLE_TYPE = "ARTICLE"

data class MyPostsState(
    val recipes: List<PostCardState> = emptyList(),
    val articles: List<PostCardState> = emptyList(),
    val savedPosts: List<PostCardState> = emptyList(),
    val currentUserId: Long? = null,
    val errorMessage: String? = null,
    val isUsingFallback: Boolean = false
)

class MyPostsViewModel(application: Application) : AndroidViewModel(application) {
    private val container = AppContainer(application)
    private val currentUserId = MutableStateFlow<Long?>(null)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val isUsingFallback = MutableStateFlow(false)

    val state: StateFlow<MyPostsState> = combine(
        container.observeRecipesFeedUseCase(),
        container.observeArticlesFeedUseCase(),
        currentUserId,
        errorMessage,
        isUsingFallback
    ) { recipes, articles, userId, error, fallback ->
        val recipePosts = recipes.map { it.toUi(RECIPE_TYPE) }
        val articlePosts = articles.map { it.toUi(ARTICLE_TYPE) }
        MyPostsState(
            recipes = recipePosts.filterByAuthor(userId),
            articles = articlePosts.filterByAuthor(userId),
            savedPosts = (recipePosts + articlePosts).filter { it.isLiked },
            currentUserId = userId,
            errorMessage = error,
            isUsingFallback = fallback
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MyPostsState())

    init {
        retryRefresh()
    }

    fun retryRefresh() {
        viewModelScope.launch {
            currentUserId.value = container.feedRepository.currentUserId()
            val recipesResult = container.feedRepository.loadRecipes()
            val articlesResult = container.feedRepository.loadArticles()
            val usesCache = recipesResult.isFromCache || articlesResult.isFromCache
            isUsingFallback.value = usesCache
            errorMessage.value = if (usesCache) {
                "Сервер недоступен. Показан офлайн-кеш."
            } else {
                null
            }
        }
    }

    fun onTagClick(postId: Int, tagId: Int) = Unit

    fun onLikeClick(postId: Int) {
        val current = state.value.allPosts().firstOrNull { it.id == postId } ?: return
        viewModelScope.launch {
            val success = container.feedRepository.toggleLike(postId, !current.isLiked)
            if (!success) {
                errorMessage.value = "Не удалось синхронизировать лайк с сервером."
            }
        }
    }

    private fun MyPostsState.allPosts(): List<PostCardState> = recipes + articles + savedPosts

    private fun List<PostCardState>.filterByAuthor(userId: Long?): List<PostCardState> {
        val userIdText = userId?.toString() ?: return emptyList()
        return filter { it.authorId == userIdText }
    }

    private fun FeedItem.toUi(type: String): PostCardState = PostCardState(
        id = id,
        type = type,
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
