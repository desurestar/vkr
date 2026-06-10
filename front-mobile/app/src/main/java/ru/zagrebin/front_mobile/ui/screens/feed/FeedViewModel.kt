package ru.zagrebin.front_mobile.ui.screens.feed

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
import ru.zagrebin.front_mobile.ui.data.RecipeRepository
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagState

private const val INITIAL_PAGE_SIZE = 10
private const val NEXT_PAGE_SIZE = 5
private const val SEARCH_DEBOUNCE_MS = 400L
private const val TAG_SUGGESTIONS_LIMIT = 10

private data class PagingState(
    val isLoadingNextPage: Boolean = false,
    val hasMorePages: Boolean = true,
    val isUsingFallback: Boolean = false
)

private data class FeedDecorations(
    val users: List<UserProfileDto>,
    val filters: FeedFilters,
    val tagQuery: String,
    val tagSuggestions: List<TagState>
)

class FeedViewModel(application: Application) : AndroidViewModel(application) {
    private val container = AppContainer(application)
    private val query = MutableStateFlow("")
    private val posts = MutableStateFlow<List<FeedItem>>(emptyList())
    private val errorMessage = MutableStateFlow<String?>(null)
    private val userResults = MutableStateFlow<List<UserProfileDto>>(emptyList())
    private val filters = MutableStateFlow(FeedFilters())
    private val tagQuery = MutableStateFlow("")
    private val tagSuggestions = MutableStateFlow<List<TagState>>(emptyList())
    private val pagingState = MutableStateFlow(PagingState())
    private var nextPage = 0
    private var searchJob: Job? = null

    private val decorations = combine(
        userResults,
        filters,
        tagQuery,
        tagSuggestions
    ) { users, currentFilters, currentTagQuery, currentTagSuggestions ->
        FeedDecorations(users, currentFilters, currentTagQuery, currentTagSuggestions)
    }

    val state: StateFlow<FeedState> = combine(
        posts,
        query,
        errorMessage,
        decorations,
        pagingState
    ) { loadedPosts, q, error, currentDecorations, paging ->
        FeedState(
            posts = loadedPosts.map { it.toUi() },
            searchQuery = q,
            errorMessage = error,
            isUsingFallback = paging.isUsingFallback,
            isLoadingNextPage = paging.isLoadingNextPage,
            hasMorePages = paging.hasMorePages,
            userResults = currentDecorations.users.map { it.toUserSearchState() },
            isUserSearch = q.trimStart().startsWith("@"),
            filters = currentDecorations.filters,
            tagQuery = currentDecorations.tagQuery,
            tagSuggestions = currentDecorations.tagSuggestions
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
            val result = container.feedRepository.loadRecipesPage(query.value, nextPage, NEXT_PAGE_SIZE, filters.value)
            if (result.isFromCache) {
                errorMessage.value = "Сервер недоступен. Показан офлайн-кеш."
                pagingState.value = pagingState.value.copy(hasMorePages = false, isUsingFallback = true)
            } else {
                val merged = (posts.value + result.data).distinctBy { it.id }
                posts.value = merged
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
        filters.value = nextFilters
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

    private fun loadUsers(searchQuery: String, debounce: Boolean = false) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (debounce) delay(SEARCH_DEBOUNCE_MS)
            posts.value = emptyList()
            pagingState.value = PagingState(hasMorePages = false)
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
            val cached = container.feedRepository.getCachedRecipesPage(searchQuery, nextPage, INITIAL_PAGE_SIZE, filters.value)
            if (cached.isNotEmpty()) {
                posts.value = cached
                pagingState.value = pagingState.value.copy(isUsingFallback = true, hasMorePages = false)
                errorMessage.value = "Показан офлайн-кеш. Обновление выполняется в фоне."
            }
            val result = container.feedRepository.loadRecipesPage(searchQuery, nextPage, INITIAL_PAGE_SIZE, filters.value)
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


private fun FeedItem.withOptimisticLike(nextLiked: Boolean): FeedItem {
    val currentLikes = likes.toIntOrNull() ?: 0
    val delta = when {
        nextLiked && !isLiked -> 1
        !nextLiked && isLiked -> -1
        else -> 0
    }
    return copy(isLiked = nextLiked, likes = (currentLikes + delta).coerceAtLeast(0).toString())
}
