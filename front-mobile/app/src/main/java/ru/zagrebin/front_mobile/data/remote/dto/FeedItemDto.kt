package ru.zagrebin.front_mobile.data.remote.dto

data class FeedItemDto(
    val id: Int,
    val authorId: String,
    val authorName: String,
    val authorHandle: String,
    val date: String,
    val title: String,
    val imageUrl: String,
    val likes: String,
    val time: String,
    val calories: String,
    val views: String
)