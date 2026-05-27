package ru.zagrebin.front_mobile.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.zagrebin.front_mobile.data.local.dao.ArticleDetailsDao
import ru.zagrebin.front_mobile.data.local.dao.FeedDao
import ru.zagrebin.front_mobile.data.local.dao.RecipeDetailsDao
import ru.zagrebin.front_mobile.data.local.entities.ArticleDetailsEntity
import ru.zagrebin.front_mobile.data.local.entities.FeedItemEntity
import ru.zagrebin.front_mobile.data.local.entities.RecipeDetailsEntity
import ru.zagrebin.front_mobile.data.remote.api.FeedApi
import ru.zagrebin.front_mobile.data.remote.dto.ArticleDetailsDto
import ru.zagrebin.front_mobile.data.remote.dto.FeedItemDto
import ru.zagrebin.front_mobile.data.remote.dto.RecipeDetailsDto
import ru.zagrebin.front_mobile.domain.model.ArticleDetails
import ru.zagrebin.front_mobile.domain.model.FeedItem
import ru.zagrebin.front_mobile.domain.model.RecipeDetails
import ru.zagrebin.front_mobile.domain.model.RecipeIngredient
import ru.zagrebin.front_mobile.domain.model.RecipeStep
import ru.zagrebin.front_mobile.domain.model.RecipeTag
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class FeedRepository(
    private val feedDao: FeedDao,
    private val feedApi: FeedApi,
    private val recipeDetailsDao: RecipeDetailsDao,
    private val articleDetailsDao: ArticleDetailsDao
) {
    fun observeRecipes(): Flow<List<FeedItem>> = feedDao.observeByType(TYPE_RECIPE).map { it.toDomain() }
    fun observeArticles(): Flow<List<FeedItem>> = feedDao.observeByType(TYPE_ARTICLE).map { it.toDomain() }

    suspend fun refreshRecipes(): RefreshResult = runCatching {
        feedDao.upsertAll(feedApi.getRecipesFeed().map { it.toEntity(TYPE_RECIPE) })
        RefreshResult.Success
    }.getOrElse { RefreshResult.Fallback }

    suspend fun refreshArticles(): RefreshResult = runCatching {
        feedDao.upsertAll(feedApi.getArticlesFeed().map { it.toEntity(TYPE_ARTICLE) })
        RefreshResult.Success
    }.getOrElse { RefreshResult.Fallback }

    fun observeRecipeDetails(id: Int): Flow<RecipeDetails?> =
        recipeDetailsDao.observeById(id).map { it?.toDomain() }

    fun observeArticleDetails(id: Int): Flow<ArticleDetails?> =
        articleDetailsDao.observeById(id).map { it?.toDomain() }

    suspend fun refreshRecipeDetails(id: Int): RefreshResult = runCatching {
        recipeDetailsDao.upsert(feedApi.getRecipeDetails(id).toRecipeDetailsEntity())
        RefreshResult.Success
    }.getOrElse { RefreshResult.Fallback }

    suspend fun refreshArticleDetails(id: Int): RefreshResult = runCatching {
        articleDetailsDao.upsert(feedApi.getArticleDetails(id).toArticleDetailsEntity())
        RefreshResult.Success
    }.getOrElse { RefreshResult.Fallback }

    private fun List<FeedItemEntity>.toDomain(): List<FeedItem> = map {
        FeedItem(it.id, it.authorId, it.authorName, it.authorHandle, it.date, it.title, it.imageUrl, it.likes, it.time, it.calories, it.views)
    }

    private fun RecipeDetailsEntity.toDomain(): RecipeDetails = RecipeDetails(
        id = id,
        authorId = authorId,
        authorName = authorName,
        authorHandle = authorHandle,
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

private fun FeedItemDto.toEntity(type: String): FeedItemEntity =
    FeedItemEntity(
        id = id,
        type = type,
        authorId = authorId.asString(),
        authorName = authorName.orEmpty(),
        authorHandle = authorHandle.orEmpty(),
        date = formatDate(date.orEmpty()),
        title = title.orEmpty(),
        imageUrl = imageUrl.orEmpty(),
        likes = formatCount(likes),
        time = formatMinutes(time),
        calories = formatCalories(calories),
        views = formatCount(views)
    )

private fun RecipeDetailsDto.toRecipeDetailsEntity(): RecipeDetailsEntity = RecipeDetailsEntity(
    id = id,
    authorId = authorId,
    authorName = authorName,
    authorHandle = authorHandle,
    date = formatDate(date),
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
    tags = tags.map { RecipeTag(it.id, it.name) },
    ingredients = ingredients.map { RecipeIngredient(it.text) },
    steps = steps.map { RecipeStep(it.id, it.title, it.description, it.imageUrl) }
)

private fun ArticleDetailsDto.toArticleDetailsEntity(): ArticleDetailsEntity = ArticleDetailsEntity(
    id = id,
    authorId = authorId,
    authorName = authorName,
    authorHandle = authorHandle,
    date = formatDate(date),
    title = title,
    imageUrl = imageUrl,
    likes = likes,
    views = views,
    content = content,
    isSaved = isSaved
)

private fun formatDate(value: String): String = runCatching {
    OffsetDateTime.parse(value).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
}.getOrDefault(value)

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

enum class RefreshResult {
    Success,
    Fallback
}
