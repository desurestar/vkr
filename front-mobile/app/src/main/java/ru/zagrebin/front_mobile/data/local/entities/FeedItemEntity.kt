package ru.zagrebin.front_mobile.data.local.entities

import androidx.room.Entity

@Entity(tableName = "feed_items", primaryKeys = ["id", "type"])
data class FeedItemEntity(
    val id: Int,
    val type: String,
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