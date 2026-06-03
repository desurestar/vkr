package ru.zagrebin.front_mobile.data.remote.dto

data class FeedItemDto(
    val id: Int = 0,
    val type: String? = null,
    val authorId: Any? = null,
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
    val cookTimeMinutes: Int? = null,
    val proteinsPer100: Double? = null,
    val fatsPer100: Double? = null,
    val carbsPer100: Double? = null,
    val kcalPer100: Double? = null,
    val isSaved: Boolean = false,
    val tags: List<TagDto> = emptyList()
)
