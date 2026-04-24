package ru.zagrebin.front_mobile.ui.screens.publicProfile.data

import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState

data class PublicProfileData(
    val userId: String,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val followingCount: Int,
    val followersCount: Int,
    val isFollowing: Boolean,
    val posts: List<PostCardState>
)

interface PublicProfileRepository {
    suspend fun getPublicProfile(userId: String): PublicProfileData
    suspend fun setFollowState(userId: String, isFollowing: Boolean): Boolean
}

