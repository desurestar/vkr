package ru.zagrebin.front_mobile.ui.screens.feed

import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState

data class UserSearchState(
    val id: Long,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null
)

data class FeedState(
    val posts: List<PostCardState> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val isUsingFallback: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val hasMorePages: Boolean = true,
    val userResults: List<UserSearchState> = emptyList(),
    val isUserSearch: Boolean = false
)
