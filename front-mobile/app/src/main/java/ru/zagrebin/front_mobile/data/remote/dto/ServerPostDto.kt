package ru.zagrebin.front_mobile.data.remote.dto


data class ServerPostDto(
    val id: Int,
    val authorId: Long,
    val type: String,
    val title: String,
    val summary: String? = null,
    val content: String? = null,
    val likes: Int,
    val createdAt: String,
    val views: Int = 0,
    val cookTimeMinutes: Int? = null,
    val tags: List<String> = emptyList()
)
