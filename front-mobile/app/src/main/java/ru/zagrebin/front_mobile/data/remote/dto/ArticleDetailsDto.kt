package ru.zagrebin.front_mobile.data.remote.dto

data class ArticleDetailsDto(
    val id: Int,
    val authorId: String,
    val authorName: String,
    val authorHandle: String,
    val date: String,
    val title: String,
    val imageUrl: String,
    val likes: String,
    val views: String,
    val content: String,
    val isSaved: Boolean
)

