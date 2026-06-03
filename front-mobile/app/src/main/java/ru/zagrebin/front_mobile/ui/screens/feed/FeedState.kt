package ru.zagrebin.front_mobile.ui.screens.feed

import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagState

data class UserSearchState(
    val id: Long,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null
)

data class FeedFilters(
    val minTime: String = "",
    val maxTime: String = "",
    val minCalories: String = "",
    val maxCalories: String = "",
    val minProteins: String = "",
    val maxProteins: String = "",
    val minFats: String = "",
    val maxFats: String = "",
    val minCarbs: String = "",
    val maxCarbs: String = "",
    val selectedTags: List<TagState> = emptyList()
) {
    val hasActiveFilters: Boolean
        get() = listOf(
            minTime,
            maxTime,
            minCalories,
            maxCalories,
            minProteins,
            maxProteins,
            minFats,
            maxFats,
            minCarbs,
            maxCarbs
        ).any { it.isNotBlank() } || selectedTags.isNotEmpty()
}

data class FeedState(
    val posts: List<PostCardState> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val isUsingFallback: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val hasMorePages: Boolean = true,
    val userResults: List<UserSearchState> = emptyList(),
    val isUserSearch: Boolean = false,
    val filters: FeedFilters = FeedFilters(),
    val tagQuery: String = "",
    val tagSuggestions: List<TagState> = emptyList()
)
