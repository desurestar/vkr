package ru.zagrebin.front_mobile.ui.screens.articles

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
import ru.zagrebin.front_mobile.data.remote.api.UserProfileDto
import ru.zagrebin.front_mobile.domain.model.FeedItem
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagState
import ru.zagrebin.front_mobile.ui.screens.feed.FeedFilters
import ru.zagrebin.front_mobile.ui.screens.feed.FeedState
import ru.zagrebin.front_mobile.ui.screens.feed.UserSearchState

private const val INITIAL_PAGE_SIZE = 10
private const val NEXT_PAGE_SIZE = 5
private const val SEARCH_DEBOUNCE_MS = 400L
private const val TAG_SUGGESTIONS_LIMIT = 10

private data class LikeOverride(val isLiked: Boolean, val likes: String)
private data class ArticleDecorations(
    val likeOverrides: Map<Int, LikeOverride>,
    val users: List<UserProfileDto>,
    val filters: FeedFilters,
    val tagQuery: String,
    val tagSuggestions: List<TagState>
)
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
    private val userResults = MutableStateFlow<List<UserProfileDto>>(emptyList())
    private val likeOverrides = MutableStateFlow<Map<Int, LikeOverride>>(emptyMap())
    private val filters = MutableStateFlow(FeedFilters())
    private val tagQuery = MutableStateFlow("")
    private val tagSuggestions = MutableStateFlow<List<TagState>>(emptyList())
    private val pagingState = MutableStateFlow(PagingState())
    private var nextPage = 0
    private var searchJob: Job? = null

    private val searchDecorations = combine(
        likeOverrides,
        userResults,
        filters,
        tagQuery,
        tagSuggestions
    ) { overrides, users, currentFilters, currentTagQuery, currentTagSuggestions ->
        ArticleDecorations(overrides, users, currentFilters, currentTagQuery, currentTagSuggestions)
    }

    val state: StateFlow<FeedState> = combine(
        posts,
        query,
        errorMessage,
        searchDecorations,
        pagingState
    ) { loadedPosts, q, error, decorations, paging ->
        FeedState(
            posts = loadedPosts.map { it.applyLikeOverride(decorations.likeOverrides[it.id]).toUi() },
            searchQuery = q,
            errorMessage = error,
            isUsingFallback = paging.isUsingFallback,
            isLoadingNextPage = paging.isLoadingNextPage,
            hasMorePages = paging.hasMorePages,
            userResults = decorations.users.map { it.toUserSearchState() },
            isUserSearch = q.trimStart().startsWith("@"),
            filters = decorations.filters,
            tagQuery = decorations.tagQuery,
            tagSuggestions = decorations.tagSuggestions
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeedState())

    init {
        loadTagSuggestions("")
        retryRefresh()
    }

    fun retryRefresh() {
        loadFirstPage(query.value)
    }

    fun loadNextPage() {
        val paging = pagingState.value
        if (query.value.trimStart().startsWith("@")) return
        if (paging.isLoadingNextPage || !paging.hasMorePages || posts.value.isEmpty()) return
        viewModelScope.launch {
            pagingState.value = pagingState.value.copy(isLoadingNextPage = true)
            val result = container.feedRepository.loadArticlesPage(query.value, nextPage, NEXT_PAGE_SIZE, filters.value)
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
        if (newQuery.trimStart().startsWith("@")) {
            loadUsers(newQuery, debounce = true)
        } else {
            userResults.value = emptyList()
            loadFirstPage(newQuery, debounce = true)
        }
    }

    fun onFilterDraftChange(nextFilters: FeedFilters) {
        filters.value = nextFilters.copy(
            minTime = "",
            maxTime = "",
            minCalories = "",
            maxCalories = "",
            minProteins = "",
            maxProteins = "",
            minFats = "",
            maxFats = "",
            minCarbs = "",
            maxCarbs = ""
        )
    }

    fun onFilterApply() {
        loadFirstPage(query.value)
    }

    fun onFilterClear() {
        filters.value = FeedFilters()
        tagQuery.value = ""
        loadTagSuggestions("")
        loadFirstPage(query.value)
    }

    fun onTagQueryChange(newQuery: String) {
        tagQuery.value = newQuery
        loadTagSuggestions(newQuery)
    }

    fun onFilterTagAdd(tag: TagState) {
        filters.value = filters.value.copy(
            selectedTags = (filters.value.selectedTags + tag.copy(isHighlighted = true)).distinctBy { it.id }
        )
        loadTagSuggestions(tagQuery.value)
    }

    fun onFilterTagRemove(tagId: Int) {
        filters.value = filters.value.copy(selectedTags = filters.value.selectedTags.filterNot { it.id == tagId })
        loadTagSuggestions(tagQuery.value)
    }

    fun onTagClick(postId: Int, tagId: Int) {
        val tag = state.value.posts.firstOrNull { it.id == postId }?.tags?.firstOrNull { it.id == tagId } ?: return
        onFilterTagAdd(tag)
        loadFirstPage(query.value)
    }

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

    private fun loadUsers(searchQuery: String, debounce: Boolean = false) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (debounce) delay(SEARCH_DEBOUNCE_MS)
            pagingState.value = pagingState.value.copy(hasMorePages = false, isLoadingNextPage = false)
            val result = container.feedRepository.searchUsers(searchQuery.trimStart().removePrefix("@").trim())
            userResults.value = result.data
            errorMessage.value = if (result.isFromCache) "Сервер недоступен. Поиск пользователей недоступен." else null
        }
    }

    private fun loadFirstPage(searchQuery: String, debounce: Boolean = false) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (debounce) delay(SEARCH_DEBOUNCE_MS)
            pagingState.value = PagingState()
            nextPage = 0
            val cached = container.feedRepository.getCachedArticlesPage(searchQuery, nextPage, INITIAL_PAGE_SIZE, filters.value)
            if (cached.isNotEmpty()) {
                posts.value = cached
                pagingState.value = pagingState.value.copy(isUsingFallback = true, hasMorePages = false)
                errorMessage.value = "Показан офлайн-кеш. Обновление выполняется в фоне."
            }
            val result = container.feedRepository.loadArticlesPage(searchQuery, nextPage, INITIAL_PAGE_SIZE, filters.value)
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

    private fun loadTagSuggestions(searchQuery: String) {
        viewModelScope.launch {
            val cached = container.feedRepository.getCachedTags(searchQuery)
            if (cached.isNotEmpty()) {
                tagSuggestions.value = cached
                    .filterNot { tag -> filters.value.selectedTags.any { it.id == tag.id } }
                    .take(TAG_SUGGESTIONS_LIMIT)
                    .map { TagState(it.id, it.name) }
            }
            val result = container.feedRepository.loadTags(searchQuery)
            tagSuggestions.value = result.data
                .filterNot { tag -> filters.value.selectedTags.any { it.id == tag.id } }
                .take(TAG_SUGGESTIONS_LIMIT)
                .map { TagState(it.id, it.name) }
        }
    }

    private fun UserProfileDto.toUserSearchState(): UserSearchState = UserSearchState(
        id = id,
        username = username.orEmpty(),
        displayName = displayName?.takeIf { it.isNotBlank() } ?: username.orEmpty(),
        avatarUrl = avatarUrl
    )

    private fun FeedItem.toUi(): PostCardState = PostCardState(
        id = id,
        status = status,
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
