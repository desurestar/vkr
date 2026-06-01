package ru.zagrebin.front_mobile.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.zagrebin.front_mobile.data.local.dao.ArticleDetailsDao
import ru.zagrebin.front_mobile.data.local.dao.FeedDao
import ru.zagrebin.front_mobile.data.local.dao.RecipeDetailsDao
import ru.zagrebin.front_mobile.data.local.dao.TagDao
import ru.zagrebin.front_mobile.data.local.entities.ArticleDetailsEntity
import ru.zagrebin.front_mobile.data.local.entities.FeedItemEntity
import ru.zagrebin.front_mobile.data.local.entities.RecipeDetailsEntity
import ru.zagrebin.front_mobile.data.local.entities.TagEntity
import ru.zagrebin.front_mobile.data.remote.api.CreateArticleRequest
import ru.zagrebin.front_mobile.data.remote.api.CreateRecipeRequest
import ru.zagrebin.front_mobile.data.remote.api.FeedApi
import ru.zagrebin.front_mobile.data.remote.dto.ArticleDetailsDto
import ru.zagrebin.front_mobile.data.remote.dto.FeedItemDto
import ru.zagrebin.front_mobile.data.remote.dto.RecipeDetailsDto
import ru.zagrebin.front_mobile.data.remote.dto.RecipeIngredientDto
import ru.zagrebin.front_mobile.data.remote.dto.RecipeStepDto
import ru.zagrebin.front_mobile.data.sync.NetworkConnectionChecker
import ru.zagrebin.front_mobile.domain.model.ArticleDetails
import ru.zagrebin.front_mobile.domain.model.FeedItem
import ru.zagrebin.front_mobile.domain.model.RecipeDetails
import ru.zagrebin.front_mobile.domain.model.RecipeIngredient
import ru.zagrebin.front_mobile.domain.model.RecipeStep
import ru.zagrebin.front_mobile.domain.model.RecipeTag
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class FeedRepository(
    private val feedDao: FeedDao,
    private val feedApi: FeedApi,
    private val recipeDetailsDao: RecipeDetailsDao,
    private val articleDetailsDao: ArticleDetailsDao,
    private val tagDao: TagDao,
    private val networkConnectionChecker: NetworkConnectionChecker
) {
    fun observeRecipes(): Flow<List<FeedItem>> = feedDao.observeByType(TYPE_RECIPE).map { it.toDomain() }
    fun observeArticles(): Flow<List<FeedItem>> = feedDao.observeByType(TYPE_ARTICLE).map { it.toDomain() }

    suspend fun loadRecipes(): ServerFirstResult<List<FeedItem>> = loadFeedItems(TYPE_RECIPE) {
        feedApi.getRecipesFeed()
    }

    suspend fun refreshRecipes(): RefreshResult = loadRecipes().toRefreshResult()

    suspend fun loadArticles(): ServerFirstResult<List<FeedItem>> = loadFeedItems(TYPE_ARTICLE) {
        feedApi.getArticlesFeed()
    }

    suspend fun refreshArticles(): RefreshResult = loadArticles().toRefreshResult()

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

    suspend fun refreshArticleDetails(id: Int): RefreshResult = refreshFromServer {
        articleDetailsDao.upsert(feedApi.getArticleDetails(id).toArticleDetailsEntity())
    }

    suspend fun createArticle(request: CreateArticleRequest): CreateArticleResult {
        if (!networkConnectionChecker.isNetworkAvailable()) return CreateArticleResult.Fallback
        return runCatching {
            val createdArticle = feedApi.createArticle(request).toArticleDetailsEntity()
            articleDetailsDao.upsert(createdArticle)
            feedDao.upsertAll(listOf(createdArticle.toFeedItemEntity()))
            loadArticles()
            CreateArticleResult.Success(createdArticle.id)
        }.getOrElse { CreateArticleResult.Fallback }
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
        if (!networkConnectionChecker.isNetworkAvailable()) return CreateRecipeResult.Fallback
        return runCatching {
            val createdRecipe = feedApi.createRecipe(request).toRecipeDetailsEntity()
            recipeDetailsDao.upsert(createdRecipe)
            feedDao.upsertAll(listOf(createdRecipe.toFeedItemEntity()))
            loadRecipes()
            CreateRecipeResult.Success(createdRecipe.id)
        }.getOrElse { CreateRecipeResult.Fallback }
    }

    private suspend fun loadFeedItems(type: String, remoteRequest: suspend () -> List<FeedItemDto>): ServerFirstResult<List<FeedItem>> {
        val cachedItems = feedDao.getByType(type)
        if (networkConnectionChecker.isNetworkAvailable()) {
            runCatching {
                val cachedImagesById = cachedItems.associate { it.id to it.imageUrl }
                remoteRequest().map { it.toEntity(type).withFallbackImage(cachedImagesById[it.id]) }
            }.onSuccess { items ->
                feedDao.replaceByType(type, items)
                return ServerFirstResult(items.toDomain(), isFromCache = false)
            }
        }

        return ServerFirstResult(cachedItems.toDomain(), isFromCache = true)
    }

    private suspend fun refreshFromServer(syncBlock: suspend () -> Unit): RefreshResult {
        if (!networkConnectionChecker.isNetworkAvailable()) return RefreshResult.Fallback
        return runCatching {
            syncBlock()
            RefreshResult.Success
        }.getOrElse { RefreshResult.Fallback }
    }

    private fun List<FeedItemEntity>.toDomain(): List<FeedItem> = map {
        FeedItem(it.id, it.authorId, it.authorName, it.authorHandle, it.authorAvatarUrl, it.date, it.title, it.imageUrl, it.likes, it.time, it.calories, it.views)
    }

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
        time = time,
        calories = calories,
        views = views
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
        time = "",
        calories = "",
        views = views
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
        steps = steps
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
        views = views,
        content = content,
        isSaved = isSaved
    )

    private companion object {
        const val TYPE_RECIPE = "recipe"
        const val TYPE_ARTICLE = "article"
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
        time = formatMinutes(preferValue(time, cookTimeMinutes)),
        calories = formatCalories(preferValue(calories, kcalPer100)),
        views = formatViews(views)
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
    }
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
    views = formatViews(views),
    content = content.orEmpty(),
    isSaved = isSaved
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
    is String -> this
    is Number -> this.toString()
    else -> this.toString()
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
    return when {
        value.startsWith("http://", ignoreCase = true) -> value
        value.startsWith("https://", ignoreCase = true) -> value
        value.startsWith("content://", ignoreCase = true) -> value
        value.startsWith("file://", ignoreCase = true) -> value
        else -> ""
    }
}
enum class RefreshResult {
    Success,
    Fallback
}
