package ru.zagrebin.front_mobile.domain.model

data class ProfileData(
    val id: Long,
    val name: String,
    val email: String,
    val bio: String,
    val avatarUrl: String?,
    val followingCount: Int,
    val followersCount: Int
)