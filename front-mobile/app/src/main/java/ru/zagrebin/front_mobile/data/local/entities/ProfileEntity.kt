package ru.zagrebin.front_mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val email: String,
    val bio: String,
    val avatarUrl: String?,
    val followingCount: Int,
    val followersCount: Int
)
