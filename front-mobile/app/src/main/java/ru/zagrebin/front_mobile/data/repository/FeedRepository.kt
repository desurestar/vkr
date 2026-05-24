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
import ru.zagrebin.front_mobile.data.remote.api.FakeFeedApi
import ru.zagrebin.front_mobile.data.remote.dto.ArticleDetailsDto
import ru.zagrebin.front_mobile.data.remote.dto.RecipeDetailsDto
import ru.zagrebin.front_mobile.domain.model.ArticleDetails
import ru.zagrebin.front_mobile.domain.model.FeedItem
import ru.zagrebin.front_mobile.domain.model.RecipeDetails
import ru.zagrebin.front_mobile.domain.model.RecipeIngredient
import ru.zagrebin.front_mobile.domain.model.RecipeStep
import ru.zagrebin.front_mobile.domain.model.RecipeTag

class FeedRepository(
    private val feedDao: FeedDao,
    private val feedApi: FeedApi,
    private val recipeDetailsDao: RecipeDetailsDao,
    private val articleDetailsDao: ArticleDetailsDao,
    private val fallbackApi: FeedApi = FakeFeedApi()
) {
    fun observeRecipes(): Flow<List<FeedItem>> = feedDao.observeByType(TYPE_RECIPE).map { it.toDomain() }
    fun observeArticles(): Flow<List<FeedItem>> = feedDao.observeByType(TYPE_ARTICLE).map { it.toDomain() }

    suspend fun refreshRecipes(): RefreshResult = runCatching {
        feedDao.upsertAll(feedApi.getRecipesFeed().map { it.toEntity(TYPE_RECIPE) })
        RefreshResult.Success
    }.getOrElse {
        feedDao.upsertAll(fallbackApi.getRecipesFeed().map { it.toEntity(TYPE_RECIPE) })
        RefreshResult.Fallback
    }

    suspend fun refreshArticles(): RefreshResult = runCatching {
        feedDao.upsertAll(feedApi.getArticlesFeed().map { it.toEntity(TYPE_ARTICLE) })
        RefreshResult.Success
    }.getOrElse {
        feedDao.upsertAll(fallbackApi.getArticlesFeed().map { it.toEntity(TYPE_ARTICLE) })
        RefreshResult.Fallback
    }

    fun observeRecipeDetails(id: Int): Flow<RecipeDetails?> =
        recipeDetailsDao.observeById(id).map { it?.toDomain() }

    fun observeArticleDetails(id: Int): Flow<ArticleDetails?> =
        articleDetailsDao.observeById(id).map { it?.toDomain() }

    suspend fun refreshRecipeDetails(id: Int) =
        recipeDetailsDao.upsert(feedApi.getRecipeDetails(id).toEntity())

    suspend fun refreshArticleDetails(id: Int) =
        articleDetailsDao.upsert(feedApi.getArticleDetails(id).toEntity())

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

private fun ru.zagrebin.front_mobile.data.remote.dto.FeedItemDto.toEntity(type: String): FeedItemEntity =
    FeedItemEntity(
        id,
        type,
        authorId,
        authorName,
        authorHandle,
        date,
        title,
        imageUrl,
        likes,
        time,
        calories,
        views
    )

private fun RecipeDetailsDto.toEntity(): RecipeDetailsEntity = RecipeDetailsEntity(
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
    tags = tags.map { RecipeTag(it.id, it.name) },
    ingredients = ingredients.map { RecipeIngredient(it.text) },
    steps = steps.map { RecipeStep(it.id, it.title, it.description, it.imageUrl) }
)

private fun ArticleDetailsDto.toEntity(): ArticleDetailsEntity = ArticleDetailsEntity(
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

enum class RefreshResult {
    Success,
    Fallback
}
