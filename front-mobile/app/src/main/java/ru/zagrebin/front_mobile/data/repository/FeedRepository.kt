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
import kotlin.math.roundToInt

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
        date = formatDate(preferValue(date, createdAt)),
        title = title.orEmpty(),
        imageUrl = imageUrl.orEmpty(),
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
    date = formatDate(preferValue(date, createdAt)),
    title = title.orEmpty(),
    imageUrl = imageUrl.orEmpty(),
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
        RecipeStep(step.resolveId(index), step.resolveTitle(index), step.description.orEmpty(), step.imageUrl)
    }
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

enum class RefreshResult {
    Success,
    Fallback
}
