package ru.zagrebin.front_mobile.ui.components.postCard

import ru.zagrebin.front_mobile.ui.components.recipeTag.TagState

data class PostCardState(
    val id: Int = 0,
    val authorId: String = "",
    val authorName: String = "",
    val authorHandle: String = "",
    val date: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val likes: String = "",
    val time: String = "",
    val calories: String = "",
    val views: String = "",
    val tags: List<TagState> = emptyList(),
    val ingredients: List<RecipeIngredientState> = emptyList(),
    val steps: List<RecipeStepState> = emptyList()
)

data class RecipeIngredientState(
    val text: String = ""
)

data class RecipeStepState(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null
)
