package ru.zagrebin.front_mobile.data.remote.api

import kotlinx.coroutines.delay
import ru.zagrebin.front_mobile.data.remote.dto.*

class FakeFeedApi : FeedApi {
    override suspend fun getRecipesFeed(): List<FeedItemDto> {
        delay(600)
        return sample(0, "Рецепт")
    }

    override suspend fun getArticlesFeed(): List<FeedItemDto> {
        delay(600)
        return sample(100, "Статья")
    }

    override suspend fun getRecipeDetails(id: Int): RecipeDetailsDto {
        delay(400)
        return sampleRecipeDetails(id)
    }

    override suspend fun getArticleDetails(id: Int): ArticleDetailsDto {
        delay(400)
        return sampleArticleDetails(id)
    }

    private fun sample(offset: Int, prefix: String): List<FeedItemDto> = List(10) { index ->
        FeedItemDto(
            id = offset + index,
            authorId = if (index % 2 == 0) "42" else "77",
            authorName = "Дмитрий Загребин",
            authorHandle = "@Dima123",
            date = "22.05.2026",
            title = "$prefix #${index + 1}: реальные данные через API",
            imageUrl = "https://img.freepik.com/premium-photo/salad-with-mixed-seafood-dark-plate_102375-5144.jpg?semt=ais_hybrid&w=740",
            likes = "${30 + index}k",
            time = "${20 + index} мин",
            calories = "${200 + index * 5} ккал",
            views = "${50 + index}k"
        )
    }

    private fun sampleRecipeDetails(id: Int): RecipeDetailsDto = RecipeDetailsDto(
        id = id,
        authorId = "42",
        authorName = "Дмитрий Загребин",
        authorHandle = "@Dima123",
        date = "22.05.2026",
        title = "Рецепт #$id: Реальные данные",
        imageUrl = "https://img.freepik.com/premium-photo/salad-with-mixed-seafood-dark-plate_102375-5144.jpg?semt=ais_hybrid&w=740",
        likes = "38.6k",
        time = "35 мин",
        calories = "250 ккал",
        views = "53.7k",
        isSaved = id % 2 == 0,
        proteinsPer100 = 9.5f,
        fatsPer100 = 6.2f,
        carbsPer100 = 18.4f,
        kcalPer100 = 145,
        tags = listOf(
            RecipeTagDto(1, "#быстро"),
            RecipeTagDto(2, "#ужин"),
            RecipeTagDto(3, "#острое")
        ),
        ingredients = listOf(
            RecipeIngredientDto("Творог - 500 г"),
            RecipeIngredientDto("Яйцо - 1 шт"),
            RecipeIngredientDto("Голубика - 150 г")
        ),
        steps = listOf(
            RecipeStepDto(1, "Шаг 1", "Подготовьте ингредиенты.", null),
            RecipeStepDto(2, "Шаг 2", "Смешайте продукты до однородности.", null),
            RecipeStepDto(3, "Шаг 3", "Обжарьте до готовности.", null)
        )
    )

    private fun sampleArticleDetails(id: Int): ArticleDetailsDto = ArticleDetailsDto(
        id = id,
        authorId = "77",
        authorName = "Анна Калинина",
        authorHandle = "@anna",
        date = "20.05.2026",
        title = "Статья #$id: Полезные привычки",
        imageUrl = "https://img.freepik.com/free-photo/healthy-food-wooden-table_144627-23687.jpg",
        likes = "12.4k",
        views = "18.2k",
        content = "Полный текст статьи будет здесь. Сервер вернет основные блоки, списки и советы.",
        isSaved = id % 2 == 1
    )
}