package ru.zagrebin.front_mobile.domain.model

data class FeedItem(
    val id: Int,
    val status: String = "PUBLISHED",
    val authorId: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String?,
    val date: String,
    val title: String,
    val imageUrl: String,
    val likes: String,
    val isLiked: Boolean,
    val time: String,
    val calories: String,
    val views: String,
    val isSaved: Boolean = false,
    val proteinsPer100: Float = 0f,
    val fatsPer100: Float = 0f,
    val carbsPer100: Float = 0f,
    val kcalPer100: Int = 0,
    val tags: List<RecipeTag> = emptyList()
)