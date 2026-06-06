package ru.zagrebin.front_mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_statistics_ops")
data class PendingStatisticsOpEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val opType: String,
    val payloadJson: String,
    val localMealId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_drafts")
data class LocalDraftEntity(
    @PrimaryKey val id: Int,
    val type: String,
    val title: String,
    val summary: String,
    val content: String,
    val imageUrl: String?,
    val requestJson: String,
    val createdAt: Long = System.currentTimeMillis()
)
