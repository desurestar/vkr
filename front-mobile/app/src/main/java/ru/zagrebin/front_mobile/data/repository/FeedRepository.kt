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
import ru.zagrebin.front_mobile.data.remote.dto.ServerPostDto
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

private fun ServerPostDto.toEntity(type: String): FeedItemEntity =
    FeedItemEntity(
        id = id,
        type = type,
        authorId = authorId.toString(),
        authorName = "Пользователь #$authorId",
        authorHandle = "@user$authorId",
        date = formatDate(createdAt),
        title = title,
        imageUrl = "",
        likes = likes.toString(),
        time = "${cookTimeMinutes ?: 0} мин",
        calories = "-",
        views = "-"
    )

private fun ServerPostDto.toRecipeDetailsEntity(): RecipeDetailsEntity = RecipeDetailsEntity(
    id = id,
    authorId = authorId.toString(),
    authorName = "Пользователь #$authorId",
    authorHandle = "@user$authorId",
    date = formatDate(createdAt),
    title = title,
    imageUrl = "",
    likes = likes.toString(),
    time = "${cookTimeMinutes ?: 0} мин",
    calories = "-",
    views = "-",
    isSaved = false,
    proteinsPer100 = 0f,
    fatsPer100 = 0f,
    carbsPer100 = 0f,
    kcalPer100 = 0,
    tags = tags.mapIndexed { index, tag -> RecipeTag(index + 1, tag) },
    ingredients = listOf(RecipeIngredient(summary ?: "")),
    steps = listOf(RecipeStep(1, "Приготовление", content ?: "", null))
)

private fun ServerPostDto.toArticleDetailsEntity(): ArticleDetailsEntity = ArticleDetailsEntity(
    id = id,
    authorId = authorId.toString(),
    authorName = "Пользователь #$authorId",
    authorHandle = "@user$authorId",
    date = formatDate(createdAt),
    title = title,
    imageUrl = "",
    likes = likes.toString(),
    views = "-",
    content = content ?: summary.orEmpty(),
    isSaved = false
)

private fun formatDate(value: String): String = runCatching {
    OffsetDateTime.parse(value).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
}.getOrDefault(value)

enum class RefreshResult {
    Success,
    Fallback
}
