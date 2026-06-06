package ru.zagrebin.front_mobile.domain.model

data class RecipeDetails(
    val id: Int,
    val authorId: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String?,
    val date: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val likes: String,
    val isLiked: Boolean,
    val time: String,
    val calories: String,
    val views: String,
    val isSaved: Boolean,
    val proteinsPer100: Float,
    val fatsPer100: Float,
    val carbsPer100: Float,
    val kcalPer100: Int,
    val tags: List<RecipeTag>,
    val ingredients: List<RecipeIngredient>,
    val steps: List<RecipeStep>,
    val comments: List<PostComment> = emptyList()
)

data class RecipeTag(
    val id: Int,
    val name: String
)

data class RecipeIngredient(
    val text: String
)

data class RecipeStep(
    val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String?
)


data class PostComment(
    val id: Long,
    val authorId: Long,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String?,
    val parentId: Long?,
    val parentAuthorName: String?,
    val text: String,
    val createdAt: String
)
