package ru.zagrebin.front_mobile.data.remote.dto


data class ArticleDetailsDto(
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
    val views: Any? = null,
    val content: String? = null,
    val isSaved: Boolean = false,
    val tags: List<RecipeTagDto> = emptyList(),
    val comments: List<CommentDto> = emptyList()
)
