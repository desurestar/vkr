package ru.zagrebin.front_mobile.data.remote.dto

data class RecipeDetailsDto(
    val id: Int,
    val authorId: String,
    val authorName: String,
    val authorHandle: String,
    val date: String,
    val title: String,
    val imageUrl: String,
    val likes: String,
    val time: String,
    val calories: String,
    val views: String,
    val isSaved: Boolean,
    val proteinsPer100: Float,
    val fatsPer100: Float,
    val carbsPer100: Float,
    val kcalPer100: Int,
    val tags: List<RecipeTagDto>,
    val ingredients: List<RecipeIngredientDto>,
    val steps: List<RecipeStepDto>
)

data class RecipeTagDto(
    val id: Int,
    val name: String
)

data class RecipeIngredientDto(
    val text: String
)

data class RecipeStepDto(
    val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String?
)

