package ru.zagrebin.front_mobile.ui.screens.publicProfile

import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState

data class PublicProfileUiState(
    val userId: String = "",
    val name: String = "",
    val handle: String = "",
    val avatarUrl: String? = null,
    val followingCount: String = "0",
    val followersCount: String = "0",
    val followingCountValue: Int = 0,
    val followersCountValue: Int = 0,
    val isFollowing: Boolean = false,
    val isOwnProfile: Boolean = false,
    val posts: List<PostCardState> = emptyList(),
    val isLoading: Boolean = false,
    val isFollowUpdating: Boolean = false,
    val error: String? = null
)

