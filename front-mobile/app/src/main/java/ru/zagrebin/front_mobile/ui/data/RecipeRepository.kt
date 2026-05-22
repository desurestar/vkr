package ru.zagrebin.front_mobile.ui.data

import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.components.postCard.RecipeIngredientState
import ru.zagrebin.front_mobile.ui.components.postCard.RecipeStepState
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagState

object RecipeRepository {
    private const val CURRENT_USER_ID = "42"

    private val posts = List(5) { index ->
        PostCardState(
            id = index,
            authorId = if (index % 2 == 0) CURRENT_USER_ID else "77",
            authorName = "Дмитрий Загребин",
            authorHandle = "@Dima123",
            date = "24.03.2026",
            title = "Токпокки (Tteokbokki) — классический рецепт",
            imageUrl = "https://img.freepik.com/premium-photo/salad-with-mixed-seafood-dark-plate_102375-5144.jpg?semt=ais_hybrid&w=740",
            likes = "38.6k",
            time = "35 мин",
            calories = "250 ккал",
            views = "53.7k",
            isSaved = index % 3 == 0,
            proteinsPer100 = 9.5f,
            fatsPer100 = 6.2f,
            carbsPer100 = 18.4f,
            kcalPer100 = 145,
            tags = listOf(
                TagState(1, "#tteokbokki"),
                TagState(2, "#корея"),
                TagState(3, "#острое"),
                TagState(4, "#streetfood")
            ),
            ingredients = listOf(
                RecipeIngredientState("Творог - 500 г"),
                RecipeIngredientState("Яйцо - 1 шт"),
                RecipeIngredientState("Голубика - 150 г"),
                RecipeIngredientState("Сахар - 2 ст. л."),
                RecipeIngredientState("Мука - 3-4 ст. л."),
                RecipeIngredientState("Разрыхлитель - 0,5 ч. л."),
                RecipeIngredientState("Ванилин - 1 щепотка")
            ),
            steps = listOf(
                RecipeStepState(
                    id = 1,
                    title = "Шаг 1",
                    description = "Подготовьте ингредиенты и обсушите ягоды бумажным полотенцем.",
                    imageUrl = "https://img.freepik.com/premium-photo/ingredients-cooking_23-2148824466.jpg"
                ),
                RecipeStepState(
                    id = 2,
                    title = "Шаг 2",
                    description = "Смешайте творог, яйцо, сахар и ванилин до однородной массы.",
                    imageUrl = "https://img.freepik.com/free-photo/cottage-cheese-bowl_114579-10256.jpg"
                ),
                RecipeStepState(
                    id = 3,
                    title = "Шаг 3",
                    description = "Добавьте муку, разрыхлитель и аккуратно вмешайте голубику.",
                    imageUrl = null
                ),
                RecipeStepState(
                    id = 4,
                    title = "Шаг 4",
                    description = "Сформируйте сырники и обжарьте на среднем огне до румяной корочки.",
                    imageUrl = "https://img.freepik.com/free-photo/pancakes-pan_23-2148743818.jpg"
                )
            )
        )
    }

    fun getPosts(): List<PostCardState> = posts

    fun getCurrentUserId(): String = CURRENT_USER_ID

    fun getMyPosts(userId: String = CURRENT_USER_ID): List<PostCardState> =
        posts.filter { it.authorId == userId }

    fun getSavedPosts(userId: String = CURRENT_USER_ID): List<PostCardState> =
        posts.filter { it.isSaved && it.authorId != userId }

    fun getAllPosts(): List<PostCardState> = posts

    fun getPostById(postId: Int): PostCardState? = posts.firstOrNull { it.id == postId }
}
