package ru.zagrebin.front_mobile.domain.model

data class ArticleDetails(
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
    val views: String,
    val content: String,
    val isSaved: Boolean,
    val tags: List<RecipeTag>,
    val comments: List<PostComment> = emptyList()
)

