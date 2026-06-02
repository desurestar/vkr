package ru.zagrebin.front_mobile.data.remote.dto

data class RecipeDetailsDto(
    val id: Int = 0,
    val authorId: Any? = null,
    val type: String? = null,
    val authorName: String? = null,
    val authorHandle: String? = null,
    val authorAvatarUrl: String? = null,
    val date: String? = null,
    val createdAt: String? = null,
    val status: String? = null,
    val title: String? = null,
    val imageUrl: String? = null,
    val likes: Any? = null,
    val likedByMe: Boolean = false,
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
    val steps: List<RecipeStepDto> = emptyList(),
    val comments: List<CommentDto> = emptyList()
)

data class CommentDto(
    val id: Long = 0,
    val authorId: Long = 0,
    val authorName: String? = null,
    val authorHandle: String? = null,
    val authorAvatarUrl: String? = null,
    val parentId: Long? = null,
    val parentAuthorName: String? = null,
    val text: String? = null,
    val createdAt: String? = null
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
