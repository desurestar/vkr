package ru.zagrebin.front_mobile.ui.screens.feed

import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState

data class FeedState(
    val posts: List<PostCardState> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val isUsingFallback: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val hasMorePages: Boolean = true
)
