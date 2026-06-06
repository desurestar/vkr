package ru.zagrebin.front_mobile.data.repository

import ru.zagrebin.front_mobile.data.AppContainer
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import ru.zagrebin.front_mobile.data.local.dao.ArticleDetailsDao
import ru.zagrebin.front_mobile.data.local.dao.FeedDao
import ru.zagrebin.front_mobile.data.local.dao.SyncDao
import ru.zagrebin.front_mobile.data.local.dao.RecipeDetailsDao
import ru.zagrebin.front_mobile.data.local.dao.TagDao
import ru.zagrebin.front_mobile.data.local.entities.ArticleDetailsEntity
import ru.zagrebin.front_mobile.data.local.entities.FeedItemEntity
import ru.zagrebin.front_mobile.data.local.entities.LocalDraftEntity
import ru.zagrebin.front_mobile.data.local.entities.RecipeDetailsEntity
import ru.zagrebin.front_mobile.data.local.entities.TagEntity
import ru.zagrebin.front_mobile.data.remote.api.CommentRequest
import ru.zagrebin.front_mobile.data.remote.api.CreateArticleRequest
import ru.zagrebin.front_mobile.data.remote.api.CreateRecipeRequest
import ru.zagrebin.front_mobile.data.remote.api.FeedApi
import ru.zagrebin.front_mobile.data.remote.api.PostViewRequest
import ru.zagrebin.front_mobile.data.remote.dto.ArticleDetailsDto
import ru.zagrebin.front_mobile.data.remote.dto.CommentDto
import ru.zagrebin.front_mobile.data.remote.dto.FeedItemDto
import ru.zagrebin.front_mobile.data.remote.dto.RecipeDetailsDto
import ru.zagrebin.front_mobile.data.remote.dto.RecipeIngredientDto
import ru.zagrebin.front_mobile.data.remote.dto.RecipeStepDto
import ru.zagrebin.front_mobile.data.remote.dto.RecipeTagDto
import ru.zagrebin.front_mobile.data.remote.dto.TagDto
import ru.zagrebin.front_mobile.data.remote.api.UserProfileDto
import ru.zagrebin.front_mobile.data.sync.NetworkConnectionChecker
import ru.zagrebin.front_mobile.domain.model.ArticleDetails
import ru.zagrebin.front_mobile.domain.model.FeedItem
import ru.zagrebin.front_mobile.domain.model.PostComment
import ru.zagrebin.front_mobile.domain.model.RecipeDetails
import ru.zagrebin.front_mobile.domain.model.RecipeIngredient
import ru.zagrebin.front_mobile.domain.model.RecipeStep
import ru.zagrebin.front_mobile.domain.model.RecipeTag
import ru.zagrebin.front_mobile.ui.screens.feed.FeedFilters
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Date
import java.util.Locale
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class FeedRepository(
    private val feedDao: FeedDao,
    private val syncDao: SyncDao,
    private val feedApi: FeedApi,
    private val recipeDetailsDao: RecipeDetailsDao,
    private val articleDetailsDao: ArticleDetailsDao,
    private val tagDao: TagDao,
    private val networkConnectionChecker: NetworkConnectionChecker
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val recipeRequestAdapter = moshi.adapter(CreateRecipeRequest::class.java)
    private val articleRequestAdapter = moshi.adapter(CreateArticleRequest::class.java)

    fun observeRecipes(): Flow<List<FeedItem>> = feedDao.observeByType(TYPE_RECIPE).map { it.toDomain() }
    fun observeArticles(): Flow<List<FeedItem>> = feedDao.observeByType(TYPE_ARTICLE).map { it.toDomain() }

    suspend fun loadRecipes(): ServerFirstResult<List<FeedItem>> = loadFeedItems(TYPE_RECIPE) {
        feedApi.getRecipesFeed()
    }

    suspend fun refreshRecipes(): RefreshResult = loadRecipes().toRefreshResult()

    suspend fun loadRecipesPage(
        query: String,
        page: Int,
        pageSize: Int,
        filters: FeedFilters = FeedFilters()
    ): ServerFirstResult<List<FeedItem>> =
        loadFeedItemsPage(TYPE_RECIPE, page) {
            feedApi.getRecipesFeed(
                query = query.takeIf { it.isNotBlank() },
                page = page,
                size = pageSize,
                minTime = filters.minTime.toIntOrNull(),
                maxTime = filters.maxTime.toIntOrNull(),
                minCalories = filters.minCalories.toDoubleOrNull(),
                maxCalories = filters.maxCalories.toDoubleOrNull(),
                minProteins = filters.minProteins.toDoubleOrNull(),
                maxProteins = filters.maxProteins.toDoubleOrNull(),
                minFats = filters.minFats.toDoubleOrNull(),
                maxFats = filters.maxFats.toDoubleOrNull(),
                minCarbs = filters.minCarbs.toDoubleOrNull(),
                maxCarbs = filters.maxCarbs.toDoubleOrNull(),
                tags = filters.selectedTags.map { it.title }.takeIf { it.isNotEmpty() }
            )
        }

    suspend fun searchRecipes(query: String, page: Int, pageSize: Int): ServerFirstResult<List<FeedItem>> {
        if (!networkConnectionChecker.isNetworkAvailable()) {
            return ServerFirstResult(emptyList(), isFromCache = true)
        }
        return runCatching {
            feedApi.getRecipesFeed(query.takeIf { it.isNotBlank() }, page, pageSize).map { it.toEntity(TYPE_RECIPE).toDomainItem() }
        }.fold(
            onSuccess = { ServerFirstResult(it, isFromCache = false) },
            onFailure = { ServerFirstResult(emptyList(), isFromCache = true) }
        )
    }

    suspend fun loadArticles(): ServerFirstResult<List<FeedItem>> = loadFeedItems(TYPE_ARTICLE) {
        feedApi.getArticlesFeed()
    }

    suspend fun refreshArticles(): RefreshResult = loadArticles().toRefreshResult()

    suspend fun loadArticlesPage(
        query: String,
        page: Int,
        pageSize: Int,
        filters: FeedFilters = FeedFilters()
    ): ServerFirstResult<List<FeedItem>> =
        loadFeedItemsPage(TYPE_ARTICLE, page) {
            feedApi.getArticlesFeed(
                query = query.takeIf { it.isNotBlank() },
                page = page,
                size = pageSize,
                tags = filters.selectedTags.map { it.title }.takeIf { it.isNotEmpty() }
            )
        }

    suspend fun loadDrafts(): ServerFirstResult<List<FeedItem>> {
        val localDrafts = syncDao.observeLocalDrafts().first().map { it.toFeedItem() }
        if (!networkConnectionChecker.isNetworkAvailable()) {
            return ServerFirstResult(localDrafts, isFromCache = true)
        }
        return runCatching {
            val remoteDrafts = feedApi.getDrafts().map { dto -> dto.toEntity(dto.type?.lowercase().orEmpty()).toDomainItem() }
            localDrafts + remoteDrafts
        }.fold(
            onSuccess = { ServerFirstResult(it, isFromCache = false) },
            onFailure = { ServerFirstResult(localDrafts, isFromCache = true) }
        )
    }

    suspend fun deleteDraft(postId: Int): Boolean {
        if (postId < 0) {
            syncDao.deleteLocalDraft(postId)
            return true
        }
        if (!networkConnectionChecker.isNetworkAvailable()) return false
        return runCatching {
            feedApi.deleteDraft(postId)
            true
        }.getOrDefault(false)
    }

    fun observeRecipeDetails(id: Int): Flow<RecipeDetails?> =
        recipeDetailsDao.observeById(id).map { it?.toDomain() }

    fun observeArticleDetails(id: Int): Flow<ArticleDetails?> =
        articleDetailsDao.observeById(id).map { it?.toDomain() }

    suspend fun loadRecipeDetails(id: Int): ServerFirstResult<RecipeDetails?> {
        if (networkConnectionChecker.isNetworkAvailable()) {
            runCatching {
                feedApi.getRecipeDetails(id)
                    .toRecipeDetailsEntity()
                    .withFallbackImages(recipeDetailsDao.getById(id))
            }.onSuccess { entity ->
                recipeDetailsDao.upsert(entity)
                return ServerFirstResult(entity.toDomain(), isFromCache = false)
            }
        }

        return ServerFirstResult(recipeDetailsDao.getById(id)?.toDomain(), isFromCache = true)
    }

    suspend fun refreshRecipeDetails(id: Int): RefreshResult = loadRecipeDetails(id).toRefreshResult()

    suspend fun addComment(postId: Int, text: String, parentId: Long?): Boolean {
        if (!networkConnectionChecker.isNetworkAvailable()) return false
        return runCatching {
            feedApi.addComment(postId, CommentRequest(text, parentId))
            refreshPostDetails(postId)
            true
        }.getOrDefault(false)
    }

    suspend fun deleteComment(postId: Int, commentId: Long): Boolean {
        if (!networkConnectionChecker.isNetworkAvailable()) return false
        return runCatching {
            feedApi.deleteComment(commentId)
            refreshPostDetails(postId)
            true
        }.getOrDefault(false)
    }

    suspend fun toggleLike(postId: Int, shouldLike: Boolean): Boolean {
        if (!networkConnectionChecker.isNetworkAvailable()) return false
        return runCatching {
            val updated = if (shouldLike) feedApi.like(postId) else feedApi.unlike(postId)
            upsertLikedPost(updated)
            true
        }.getOrDefault(false)
    }

    suspend fun recordPostView(postId: Int, type: String, durationSeconds: Int): Boolean {
        if (!networkConnectionChecker.isNetworkAvailable()) return false
        return runCatching {
            val updated = feedApi.recordView(postId, PostViewRequest(durationSeconds))
            val views = formatViews(updated.views)
            val normalizedType = type.lowercase()
            feedDao.updateViews(postId, normalizedType, views)
            if (normalizedType == TYPE_RECIPE) {
                recipeDetailsDao.updateViews(postId, views)
            } else {
                articleDetailsDao.updateViews(postId, views)
            }
            true
        }.getOrDefault(false)
    }

    suspend fun currentUserId(): Long? = runCatching { feedApi.me().id }.getOrNull()

    suspend fun searchUsers(query: String, page: Int = 0, pageSize: Int = 10): ServerFirstResult<List<UserProfileDto>> {
        if (!networkConnectionChecker.isNetworkAvailable()) {
            return ServerFirstResult(emptyList(), isFromCache = true)
        }
        return runCatching {
            feedApi.searchUsers(query, page, pageSize)
        }.fold(
            onSuccess = { ServerFirstResult(it, isFromCache = false) },
            onFailure = { ServerFirstResult(emptyList(), isFromCache = true) }
        )
    }

    private suspend fun refreshPostDetails(postId: Int) {
        val post = feedApi.getRecipeDetails(postId)
        if (post.type?.equals("ARTICLE", ignoreCase = true) == true) {
            articleDetailsDao.upsert(feedApi.getArticleDetails(postId).toArticleDetailsEntity())
        } else {
            recipeDetailsDao.upsert(post.toRecipeDetailsEntity())
        }
    }

    suspend fun refreshArticleDetails(id: Int): RefreshResult = refreshFromServer {
        articleDetailsDao.upsert(feedApi.getArticleDetails(id).toArticleDetailsEntity())
    }

    suspend fun createArticle(request: CreateArticleRequest): CreateArticleResult {
        if (request.status.equals(STATUS_DRAFT, ignoreCase = true) && !networkConnectionChecker.isNetworkAvailable()) {
            return saveLocalArticleDraft(request)
        }
        return runCatching {
            if (!networkConnectionChecker.isNetworkAvailable()) error("Network is unavailable")
            val createdArticle = feedApi.createArticle(request).toArticleDetailsEntity()
            articleDetailsDao.upsert(createdArticle)
            upsertSavedFeedItems(listOf(createdArticle.toFeedItemEntity()))
            loadArticles()
            CreateArticleResult.Success(createdArticle.id)
        }.getOrElse {
            if (request.status.equals(STATUS_DRAFT, ignoreCase = true)) saveLocalArticleDraft(request) else CreateArticleResult.Fallback
        }
    }

    suspend fun loadTags(query: String? = null): ServerFirstResult<List<RecipeTag>> {
        if (!networkConnectionChecker.isNetworkAvailable()) {
            return ServerFirstResult(
                tagDao.getAll()
                    .filter { query.isNullOrBlank() || it.name.contains(query, ignoreCase = true) || it.label?.contains(query, ignoreCase = true) == true }
                    .map { RecipeTag(it.id.toInt(), it.label?.takeIf(String::isNotBlank) ?: it.name) },
                isFromCache = true
            )
        }

        return runCatching { feedApi.getTags(query?.takeIf { it.isNotBlank() }) }
            .fold(
                onSuccess = { tags ->
                    if (query.isNullOrBlank()) {
                        tagDao.replaceAll(tags.map { TagEntity(it.id, it.name, it.label) })
                    }
                    ServerFirstResult(tags.toFeedRecipeTags(), isFromCache = false)
                },
                onFailure = {
                    ServerFirstResult(
                        tagDao.getAll()
                            .filter { query.isNullOrBlank() || it.name.contains(query, ignoreCase = true) || it.label?.contains(query, ignoreCase = true) == true }
                            .map { RecipeTag(it.id.toInt(), it.label?.takeIf(String::isNotBlank) ?: it.name) },
                        isFromCache = true
                    )
                }
            )
    }

    suspend fun loadTagLabels(): ServerFirstResult<List<String>> {
        if (networkConnectionChecker.isNetworkAvailable()) {
            runCatching { feedApi.getTags() }
                .onSuccess { tags ->
                    tagDao.replaceAll(tags.map { TagEntity(it.id, it.name, it.label) })
                    return ServerFirstResult(tags.map { it.label?.takeIf(String::isNotBlank) ?: it.name }, isFromCache = false)
                }
        }

        return ServerFirstResult(
            tagDao.getAll().map { it.label?.takeIf(String::isNotBlank) ?: it.name },
            isFromCache = true
        )
    }

    suspend fun createRecipe(request: CreateRecipeRequest): CreateRecipeResult {
        if (request.status.equals(STATUS_DRAFT, ignoreCase = true) && !networkConnectionChecker.isNetworkAvailable()) {
            return saveLocalRecipeDraft(request)
        }
        return runCatching {
            if (!networkConnectionChecker.isNetworkAvailable()) error("Network is unavailable")
            val createdRecipe = feedApi.createRecipe(request).toRecipeDetailsEntity()
            recipeDetailsDao.upsert(createdRecipe)
            upsertSavedFeedItems(listOf(createdRecipe.toFeedItemEntity()))
            loadRecipes()
            CreateRecipeResult.Success(createdRecipe.id)
        }.getOrElse {
            if (request.status.equals(STATUS_DRAFT, ignoreCase = true)) saveLocalRecipeDraft(request) else CreateRecipeResult.Fallback
        }
    }

    suspend fun syncLocalDrafts(): Boolean {
        if (!networkConnectionChecker.isNetworkAvailable()) return false
        val drafts = syncDao.getLocalDraftsForSync()
        for (draft in drafts) {
            val success = runCatching {
                when (draft.type) {
                    TYPE_RECIPE -> feedApi.createRecipe(recipeRequestAdapter.fromJson(draft.requestJson) ?: error("Invalid recipe draft"))
                    TYPE_ARTICLE -> feedApi.createArticle(articleRequestAdapter.fromJson(draft.requestJson) ?: error("Invalid article draft"))
                    else -> error("Unknown draft type")
                }
            }.isSuccess
            if (success) {
                syncDao.deleteLocalDraft(draft.id)
            } else {
                return false
            }
        }
        return true
    }

    private suspend fun saveLocalRecipeDraft(request: CreateRecipeRequest): CreateRecipeResult {
        val id = nextLocalDraftId()
        syncDao.upsertLocalDraft(
            LocalDraftEntity(
                id = id,
                type = TYPE_RECIPE,
                title = request.title,
                summary = request.summary,
                content = request.content,
                imageUrl = request.imageUrl,
                requestJson = recipeRequestAdapter.toJson(request.copy(status = STATUS_DRAFT))
            )
        )
        return CreateRecipeResult.Success(id)
    }

    private suspend fun saveLocalArticleDraft(request: CreateArticleRequest): CreateArticleResult {
        val id = nextLocalDraftId()
        syncDao.upsertLocalDraft(
            LocalDraftEntity(
                id = id,
                type = TYPE_ARTICLE,
                title = request.title,
                summary = request.summary,
                content = request.content,
                imageUrl = request.imageUrl,
                requestJson = articleRequestAdapter.toJson(request.copy(status = STATUS_DRAFT))
            )
        )
        return CreateArticleResult.Success(id)
    }

    private fun nextLocalDraftId(): Int = -(System.currentTimeMillis() % Int.MAX_VALUE).toInt().coerceAtLeast(1)

    private suspend fun loadFeedItems(type: String, remoteRequest: suspend () -> List<FeedItemDto>): ServerFirstResult<List<FeedItem>> {
        val cachedItems = feedDao.getByType(type)
        if (networkConnectionChecker.isNetworkAvailable()) {
            runCatching {
                val cachedImagesById = cachedItems.associate { it.id to it.imageUrl }
                remoteRequest().map { it.toEntity(type).withFallbackImage(cachedImagesById[it.id]) }
            }.onSuccess { items ->
                feedDao.replaceByType(type, items.savedForCache())
                return ServerFirstResult(items.toDomain(), isFromCache = false)
            }
        }

        return ServerFirstResult(cachedItems.toDomain(), isFromCache = true)
    }

    private suspend fun loadFeedItemsPage(
        type: String,
        page: Int,
        remoteRequest: suspend () -> List<FeedItemDto>
    ): ServerFirstResult<List<FeedItem>> {
        val cachedItems = feedDao.getByType(type)
        if (networkConnectionChecker.isNetworkAvailable()) {
            runCatching {
                val cachedImagesById = cachedItems.associate { it.id to it.imageUrl }
                remoteRequest().map { it.toEntity(type).withFallbackImage(cachedImagesById[it.id]) }
            }.onSuccess { items ->
                if (page == 0) {
                    feedDao.replaceByType(type, items.savedForCache())
                } else {
                    upsertSavedFeedItems(items.savedForCache())
                }
                return ServerFirstResult(items.toDomain(), isFromCache = false)
            }
        }

        return ServerFirstResult(if (page == 0) cachedItems.toDomain() else emptyList(), isFromCache = true)
    }

    private suspend fun refreshFromServer(syncBlock: suspend () -> Unit): RefreshResult {
        if (!networkConnectionChecker.isNetworkAvailable()) return RefreshResult.Fallback
        return runCatching {
            syncBlock()
            RefreshResult.Success
        }.getOrElse { RefreshResult.Fallback }
    }

    private suspend fun upsertLikedPost(updated: FeedItemDto) {
        val type = updated.type?.lowercase()
            ?: if (recipeDetailsDao.getById(updated.id) != null) TYPE_RECIPE else TYPE_ARTICLE
        val entity = updated.toEntity(type)
        if (entity.isLiked || entity.isSaved) {
            upsertSavedFeedItems(listOf(entity))
        } else {
            feedDao.deleteByIdAndType(entity.id, entity.type)
        }
        if (type == TYPE_RECIPE) {
            recipeDetailsDao.getById(updated.id)?.let { cached ->
                recipeDetailsDao.upsert(
                    cached.copy(
                        likes = formatCount(updated.likes),
                        isLiked = updated.likedByMe,
                        tags = updated.tags.toFeedRecipeTags().ifEmpty { cached.tags }
                    )
                )
            }
        } else {
            articleDetailsDao.getById(updated.id)?.let { cached ->
                articleDetailsDao.upsert(
                    cached.copy(
                        likes = formatCount(updated.likes),
                        isLiked = updated.likedByMe,
                        tags = updated.tags.toFeedRecipeTags().ifEmpty { cached.tags }
                    )
                )
            }
        }
    }


    private fun LocalDraftEntity.toFeedItem(): FeedItem = FeedItem(
        id = id,
        authorId = "",
        authorName = "Локальный черновик",
        authorHandle = "",
        authorAvatarUrl = null,
        date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(createdAt)),
        title = title,
        imageUrl = imageUrl.orEmpty(),
        likes = "0",
        isLiked = false,
        time = if (type == TYPE_RECIPE) "Черновик" else "",
        calories = "",
        views = "0",
        isSaved = true,
        tags = emptyList()
    )

    private suspend fun upsertSavedFeedItems(items: List<FeedItemEntity>) {
        val cachedItems = items.savedForCache()
        if (cachedItems.isNotEmpty()) {
            feedDao.upsertAll(cachedItems)
        }
    }

    private fun List<FeedItemEntity>.savedForCache(): List<FeedItemEntity> = filter { it.isLiked || it.isSaved }

    private fun List<FeedItemEntity>.toDomain(): List<FeedItem> = map { it.toDomainItem() }

    private fun FeedItemEntity.toDomainItem(): FeedItem = FeedItem(
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
        isSaved = isSaved,
        proteinsPer100 = proteinsPer100,
        fatsPer100 = fatsPer100,
        carbsPer100 = carbsPer100,
        kcalPer100 = kcalPer100,
        tags = tags
    )

    private fun RecipeDetailsEntity.toFeedItemEntity(): FeedItemEntity = FeedItemEntity(
        id = id,
        type = TYPE_RECIPE,
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
        isSaved = isSaved,
        proteinsPer100 = proteinsPer100,
        fatsPer100 = fatsPer100,
        carbsPer100 = carbsPer100,
        kcalPer100 = kcalPer100,
        tags = tags
    )

    private fun ArticleDetailsEntity.toFeedItemEntity(): FeedItemEntity = FeedItemEntity(
        id = id,
        type = TYPE_ARTICLE,
        authorId = authorId,
        authorName = authorName,
        authorHandle = authorHandle,
        authorAvatarUrl = authorAvatarUrl,
        date = date,
        title = title,
        imageUrl = imageUrl,
        likes = likes,
        isLiked = isLiked,
        time = "",
        calories = "",
        views = views,
        tags = tags
    )

    private fun RecipeDetailsEntity.toDomain(): RecipeDetails = RecipeDetails(
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
        isSaved = isSaved,
        proteinsPer100 = proteinsPer100,
        fatsPer100 = fatsPer100,
        carbsPer100 = carbsPer100,
        kcalPer100 = kcalPer100,
        tags = tags,
        ingredients = ingredients,
        steps = steps,
        comments = comments
    )

    private fun ArticleDetailsEntity.toDomain(): ArticleDetails = ArticleDetails(
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
        views = views,
        content = content,
        isSaved = isSaved,
        tags = tags,
        comments = comments
    )

    private companion object {
        const val TYPE_RECIPE = "recipe"
        const val TYPE_ARTICLE = "article"
        const val STATUS_DRAFT = "DRAFT"
    }
}

data class ServerFirstResult<T>(val data: T, val isFromCache: Boolean)

sealed class CreateArticleResult {
    data class Success(val postId: Int) : CreateArticleResult()
    data object Fallback : CreateArticleResult()
}

sealed class CreateRecipeResult {
    data class Success(val postId: Int) : CreateRecipeResult()
    data object Fallback : CreateRecipeResult()
}

private fun <T> ServerFirstResult<T>.toRefreshResult(): RefreshResult =
    if (isFromCache) RefreshResult.Fallback else RefreshResult.Success

private fun FeedItemEntity.withFallbackImage(fallbackImageUrl: String?): FeedItemEntity =
    if (imageUrl.isNotBlank() || fallbackImageUrl.isNullOrBlank()) this else copy(imageUrl = fallbackImageUrl)

private fun RecipeDetailsEntity.withFallbackImages(fallback: RecipeDetailsEntity?): RecipeDetailsEntity {
    if (fallback == null) return this

    val resolvedImageUrl = imageUrl.ifBlank { fallback.imageUrl }
    val fallbackStepImagesById = fallback.steps.associate { it.id to it.imageUrl }
    val resolvedSteps = steps.map { step ->
        if (!step.imageUrl.isNullOrBlank()) {
            step
        } else {
            step.copy(imageUrl = fallbackStepImagesById[step.id])
        }
    }

    return copy(imageUrl = resolvedImageUrl, steps = resolvedSteps)
}

private fun FeedItemDto.toEntity(type: String): FeedItemEntity =
    FeedItemEntity(
        id = id,
        type = type,
        authorId = authorId.asString(),
        authorName = authorName.orEmpty(),
        authorHandle = authorHandle.orEmpty(),
        authorAvatarUrl = authorAvatarUrl.normalizeImageUrl(),
        date = formatDate(preferValue(date, createdAt)),
        title = title.orEmpty(),
        imageUrl = imageUrl.normalizeImageUrl(),
        likes = formatCount(likes),
        isLiked = likedByMe,
        time = formatMinutes(preferValue(time, cookTimeMinutes)),
        calories = formatCalories(preferValue(calories, kcalPer100)),
        views = formatViews(views),
        isSaved = isSaved || likedByMe,
        proteinsPer100 = (proteinsPer100 ?: 0.0).toFloat(),
        fatsPer100 = (fatsPer100 ?: 0.0).toFloat(),
        carbsPer100 = (carbsPer100 ?: 0.0).toFloat(),
        kcalPer100 = (kcalPer100 ?: 0.0).roundToInt(),
        tags = tags.toFeedRecipeTags()
    )

private fun RecipeDetailsDto.toRecipeDetailsEntity(): RecipeDetailsEntity = RecipeDetailsEntity(
    id = id,
    authorId = authorId.asString(),
    authorName = authorName.orEmpty(),
    authorHandle = authorHandle.orEmpty(),
    authorAvatarUrl = authorAvatarUrl.normalizeImageUrl(),
    date = formatDate(preferValue(date, createdAt)),
    title = title.orEmpty(),
    imageUrl = imageUrl.normalizeImageUrl(steps),
    likes = formatCount(likes),
    isLiked = likedByMe,
    time = formatMinutes(preferValue(time, cookTimeMinutes)),
    calories = formatCalories(preferValue(calories, kcalPer100)),
    views = formatViews(views),
    isSaved = isSaved,
    proteinsPer100 = (proteinsPer100 ?: 0.0).toFloat(),
    fatsPer100 = (fatsPer100 ?: 0.0).toFloat(),
    carbsPer100 = (carbsPer100 ?: 0.0).toFloat(),
    kcalPer100 = (kcalPer100 ?: 0.0).roundToInt(),
    tags = tags.map { RecipeTag(it.id, it.name) },
    ingredients = ingredients.map { RecipeIngredient(it.toText()) },
    steps = steps.mapIndexed { index, step ->
        RecipeStep(step.resolveId(index), step.resolveTitle(index), step.description.orEmpty(), step.imageUrl.normalizeImageUrl())
    },
    comments = comments.map { it.toDomain() }
)

private fun ArticleDetailsDto.toArticleDetailsEntity(): ArticleDetailsEntity = ArticleDetailsEntity(
    id = id,
    authorId = authorId.asString(),
    authorName = authorName.orEmpty(),
    authorHandle = authorHandle.orEmpty(),
    authorAvatarUrl = authorAvatarUrl.normalizeImageUrl(),
    date = formatDate(preferValue(date, createdAt)),
    title = title.orEmpty(),
    imageUrl = imageUrl.normalizeImageUrl(),
    likes = formatCount(likes),
    isLiked = likedByMe,
    views = formatViews(views),
    content = content.orEmpty(),
    isSaved = isSaved,
    tags = tags.toRecipeTags(),
    comments = comments.map { it.toDomain() }
)

private fun CommentDto.toDomain(): PostComment = PostComment(
    id = id,
    authorId = authorId,
    authorName = authorName.orEmpty(),
    authorHandle = authorHandle?.takeIf { it.isNotBlank() }?.let { "@$it" }.orEmpty(),
    authorAvatarUrl = authorAvatarUrl.normalizeImageUrl(),
    parentId = parentId,
    parentAuthorName = parentAuthorName,
    text = text.orEmpty(),
    createdAt = formatDate(createdAt)
)

private fun formatDate(value: Any?): String {
    val raw = value.asString()
    if (raw.isBlank()) return ""
    return runCatching {
        OffsetDateTime.parse(raw).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }.getOrDefault(raw)
}

private fun preferValue(primary: Any?, fallback: Any?): Any? = when (primary) {
    null -> fallback
    is String -> if (primary.isBlank()) fallback else primary
    else -> primary
}

private fun Any?.asString(): String = when (this) {
    null -> ""
    is String -> this.trim().removeSuffix(".0")
    is Number -> this.toLong().toString()
    else -> this.toString().trim().removeSuffix(".0")
}

private fun formatCount(value: Any?): String = when (value) {
    null -> ""
    is String -> value
    is Number -> value.toLong().toString()
    else -> value.toString()
}

private fun formatViews(value: Any?): String = when (value) {
    null -> "0"
    is String -> if (value.isBlank()) "0" else value
    is Number -> value.toLong().toString()
    else -> value.toString()
}

private fun formatMinutes(value: Any?): String = when (value) {
    null -> ""
    is String -> value
    is Number -> "${value.toLong()} мин"
    else -> value.toString()
}

private fun formatCalories(value: Any?): String = when (value) {
    null -> ""
    is String -> value
    is Number -> "${value.toLong()} ккал"
    else -> value.toString()
}

private fun List<TagDto>.toFeedRecipeTags(): List<RecipeTag> = map {
    RecipeTag(it.id.toInt(), it.label?.takeIf(String::isNotBlank) ?: it.name)
}

private fun List<RecipeTagDto>.toRecipeTags(): List<RecipeTag> = map {
    RecipeTag(it.id, it.name)
}

private fun RecipeIngredientDto.toText(): String {
    if (!text.isNullOrBlank()) return text
    val amountText = amount?.let { formatAmount(it) }.orEmpty()
    val unitText = unit.orEmpty()
    val amountUnit = listOf(amountText, unitText).filter { it.isNotBlank() }.joinToString(" ")
    val nameText = name.orEmpty()
    return listOf(nameText, amountUnit).filter { it.isNotBlank() }.joinToString(" - ")
}

private fun RecipeStepDto.resolveId(index: Int): Int = id ?: number ?: index + 1

private fun RecipeStepDto.resolveTitle(index: Int): String = when {
    !title.isNullOrBlank() -> title
    number != null -> "Шаг $number"
    else -> "Шаг ${index + 1}"
}

private fun formatAmount(value: Double): String = if (value % 1.0 == 0.0) {
    value.toLong().toString()
} else {
    value.toString()
}


private fun String?.normalizeImageUrl(steps: List<RecipeStepDto> = emptyList()): String {
    val primary = this.cleanImageUrl()
    if (primary.isNotBlank()) return primary

    return steps.asSequence()
        .mapNotNull { it.imageUrl.cleanImageUrl().takeIf(String::isNotBlank) }
        .firstOrNull()
        .orEmpty()
}

private fun String?.cleanImageUrl(): String {
    val value = this?.trim().orEmpty()
    if (value.isBlank()) return ""
    val relative = AppContainer.toRelativeMediaPath(value) ?: return ""
    return when {
        relative.startsWith("/media/", ignoreCase = true) -> relative
        relative.startsWith("content://", ignoreCase = true) -> relative
        relative.startsWith("file://", ignoreCase = true) -> relative
        relative.startsWith("http://", ignoreCase = true) -> relative
        relative.startsWith("https://", ignoreCase = true) -> relative
        else -> ""
    }
}
enum class RefreshResult {
    Success,
    Fallback
}
