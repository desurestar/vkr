package ru.zagrebin.front_mobile.domain.model

data class FeedItem(
    val id: Int,
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
    val tags: List<RecipeTag> = emptyList()
)