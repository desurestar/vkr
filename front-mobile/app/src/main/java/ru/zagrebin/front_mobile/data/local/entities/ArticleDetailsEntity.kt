package ru.zagrebin.front_mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.zagrebin.front_mobile.domain.model.PostComment

@Entity(tableName = "article_details")
data class ArticleDetailsEntity(
    @PrimaryKey val id: Int,
    val authorId: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String?,
    val date: String,
    val title: String,
    val imageUrl: String,
    val likes: String,
    val views: String,
    val content: String,
    val isSaved: Boolean,
    val comments: List<PostComment>
)

