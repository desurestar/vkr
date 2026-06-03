package ru.zagrebin.front_mobile.ui.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
private const val INITIAL_PAGE_SIZE = 10
private const val NEXT_PAGE_SIZE = 5
private const val SEARCH_DEBOUNCE_MS = 400L

private data class MyPostsPagingState(
    val isLoadingNextPage: Boolean = false,
    val hasMoreRecipes: Boolean = true,
    val hasMoreArticles: Boolean = true,
    val isUsingFallback: Boolean = false
)

data class MyPostsState(
    val recipes: List<PostCardState> = emptyList(),
    val articles: List<PostCardState> = emptyList(),
    val savedPosts: List<PostCardState> = emptyList(),
    val currentUserId: Long? = null,
    val errorMessage: String? = null,
    val isUsingFallback: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val hasMoreRecipes: Boolean = true,
    val hasMoreArticles: Boolean = true,
    val hasMoreSavedPosts: Boolean = true,
    val searchQuery: String = ""
)

class MyPostsViewModel(application: Application) : AndroidViewModel(application) {
    private val container = AppContainer(application)
    private val recipes = MutableStateFlow<List<FeedItem>>(emptyList())
    private val articles = MutableStateFlow<List<FeedItem>>(emptyList())
    private val currentUserId = MutableStateFlow<Long?>(null)
    private val query = MutableStateFlow("")
    private val errorMessage = MutableStateFlow<String?>(null)
    private val pagingState = MutableStateFlow(MyPostsPagingState())
    private var nextRecipesPage = 0
    private var nextArticlesPage = 0
    private var searchJob: Job? = null

    private val searchMeta = combine(query, errorMessage) { searchQuery, error -> searchQuery to error }

    val state: StateFlow<MyPostsState> = combine(
        recipes,
        articles,
        currentUserId,
        searchMeta,
        pagingState
    ) { recipeItems, articleItems, userId, meta, paging ->
        val (searchQuery, error) = meta
        val recipePosts = recipeItems.map { it.toUi(RECIPE_TYPE) }
        val articlePosts = articleItems.map { it.toUi(ARTICLE_TYPE) }
        MyPostsState(
            recipes = recipePosts.filterByAuthor(userId),
            articles = articlePosts.filterByAuthor(userId),
            savedPosts = (recipePosts + articlePosts).filter { it.isLiked },
            currentUserId = userId,
            errorMessage = error,
            isUsingFallback = paging.isUsingFallback,
            isLoadingNextPage = paging.isLoadingNextPage,
            hasMoreRecipes = paging.hasMoreRecipes,
            hasMoreArticles = paging.hasMoreArticles,
            hasMoreSavedPosts = paging.hasMoreRecipes || paging.hasMoreArticles,
            searchQuery = searchQuery
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MyPostsState())

    init {
        retryRefresh()
    }

    fun retryRefresh() {
        refreshPosts(query.value)
    }

    private fun refreshPosts(searchQuery: String, debounce: Boolean = false) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (debounce) delay(SEARCH_DEBOUNCE_MS)
            currentUserId.value = container.feedRepository.currentUserId()
            nextRecipesPage = 0
            nextArticlesPage = 0
            pagingState.value = MyPostsPagingState()
            val recipesResult = container.feedRepository.loadRecipesPage(searchQuery, nextRecipesPage, INITIAL_PAGE_SIZE)
            val articlesResult = container.feedRepository.loadArticlesPage(searchQuery, nextArticlesPage, INITIAL_PAGE_SIZE)
            recipes.value = recipesResult.data
            articles.value = articlesResult.data
            nextRecipesPage = 1
            nextArticlesPage = 1
            pagingState.value = pagingState.value.copy(
                hasMoreRecipes = !recipesResult.isFromCache && recipesResult.data.size >= INITIAL_PAGE_SIZE,
                hasMoreArticles = !articlesResult.isFromCache && articlesResult.data.size >= INITIAL_PAGE_SIZE,
                isUsingFallback = recipesResult.isFromCache || articlesResult.isFromCache
            )
            val usesCache = recipesResult.isFromCache || articlesResult.isFromCache
            errorMessage.value = if (usesCache) {
                "Сервер недоступен. Показан офлайн-кеш."
            } else {
                null
            }
        }
    }

    fun loadNextRecipesPage() = loadNextPage(loadRecipes = true, loadArticles = false)

    fun loadNextArticlesPage() = loadNextPage(loadRecipes = false, loadArticles = true)

    fun onSearch(newQuery: String) {
        query.value = newQuery
        refreshPosts(newQuery, debounce = true)
    }

    fun loadNextSavedPostsPage() = loadNextPage(
        loadRecipes = pagingState.value.hasMoreRecipes,
        loadArticles = pagingState.value.hasMoreArticles
    )

    fun onTagClick(postId: Int, tagId: Int) = Unit

    fun onLikeClick(postId: Int) {
        val current = state.value.allPosts().firstOrNull { it.id == postId } ?: return
        viewModelScope.launch {
            val success = container.feedRepository.toggleLike(postId, !current.isLiked)
            if (!success) {
                errorMessage.value = "Не удалось синхронизировать лайк с сервером."
            } else {
                val update: (FeedItem) -> FeedItem = { item ->
                    if (item.id == postId) item.withOptimisticLike(!current.isLiked) else item
                }
                recipes.value = recipes.value.map(update)
                articles.value = articles.value.map(update)
            }
        }
    }

    private fun loadNextPage(loadRecipes: Boolean, loadArticles: Boolean) {
        val paging = pagingState.value
        if (paging.isLoadingNextPage) return
        if ((!loadRecipes || !paging.hasMoreRecipes) && (!loadArticles || !paging.hasMoreArticles)) return
        viewModelScope.launch {
            pagingState.value = pagingState.value.copy(isLoadingNextPage = true)
            var usesCache = false
            var loadedAny = false
            if (loadRecipes && pagingState.value.hasMoreRecipes) {
                val result = container.feedRepository.loadRecipesPage(query.value, nextRecipesPage, NEXT_PAGE_SIZE)
                usesCache = usesCache || result.isFromCache
                if (result.isFromCache) {
                    pagingState.value = pagingState.value.copy(hasMoreRecipes = false)
                } else {
                    recipes.value = (recipes.value + result.data).distinctBy { it.id }
                    nextRecipesPage += 1
                    pagingState.value = pagingState.value.copy(hasMoreRecipes = result.data.size >= NEXT_PAGE_SIZE)
                    loadedAny = loadedAny || result.data.isNotEmpty()
                }
            }
            if (loadArticles && pagingState.value.hasMoreArticles) {
                val result = container.feedRepository.loadArticlesPage(query.value, nextArticlesPage, NEXT_PAGE_SIZE)
                usesCache = usesCache || result.isFromCache
                if (result.isFromCache) {
                    pagingState.value = pagingState.value.copy(hasMoreArticles = false)
                } else {
                    articles.value = (articles.value + result.data).distinctBy { it.id }
                    nextArticlesPage += 1
                    pagingState.value = pagingState.value.copy(hasMoreArticles = result.data.size >= NEXT_PAGE_SIZE)
                    loadedAny = loadedAny || result.data.isNotEmpty()
                }
            }
            if (usesCache) {
                errorMessage.value = "Сервер недоступен. Показан офлайн-кеш."
                pagingState.value = pagingState.value.copy(isUsingFallback = true)
            } else if (loadedAny) {
                errorMessage.value = null
                pagingState.value = pagingState.value.copy(isUsingFallback = false)
            }
            pagingState.value = pagingState.value.copy(isLoadingNextPage = false)
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

private fun FeedItem.withOptimisticLike(nextLiked: Boolean): FeedItem {
    val currentLikes = likes.toIntOrNull() ?: 0
    val delta = when {
        nextLiked && !isLiked -> 1
        !nextLiked && isLiked -> -1
        else -> 0
    }
    return copy(isLiked = nextLiked, likes = (currentLikes + delta).coerceAtLeast(0).toString())
}
