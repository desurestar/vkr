package ru.zagrebin.front_mobile.data.remote.dto

data class RecipeDetailsDto(
    val id: Int = 0,
    val authorId: Any? = null,
    val authorName: String? = null,
    val authorHandle: String? = null,
    val authorAvatarUrl: String? = null,
    val date: String? = null,
    val createdAt: String? = null,
    val title: String? = null,
    val imageUrl: String? = null,
    val likes: Any? = null,
    val time: Any? = null,
    val calories: Any? = null,
    val views: Any? = null,
    val isSaved: Boolean = false,
    val proteinsPer100: Double? = null,
    val fatsPer100: Double? = null,
    val carbsPer100: Double? = null,
    val kcalPer100: Double? = null,
    val cookTimeMinutes: Int? = null,
    val tags: List<RecipeTagDto> = emptyList(),
    val ingredients: List<RecipeIngredientDto> = emptyList(),
    val steps: List<RecipeStepDto> = emptyList()
)

data class RecipeTagDto(
    val id: Int = 0,
    val name: String = ""
)

data class RecipeIngredientDto(
    val text: String? = null,
    val name: String? = null,
    val amount: Double? = null,
    val unit: String? = null
)

data class RecipeStepDto(
    val id: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val number: Int? = null
)
