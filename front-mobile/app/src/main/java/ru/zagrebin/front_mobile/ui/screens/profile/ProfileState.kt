package ru.zagrebin.front_mobile.ui.screens.profile

enum class ProfileUsersSheetType(val title: String, val emptyTitle: String) {
    Following("Подписки", "Подписок не найдено"),
    Followers("Подписчики", "Подписчиков не найдено")
}

data class ProfileUserItemState(
    val id: Long,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null
)

data class ProfileState(
    val isLoading: Boolean = false,
    val userId: Long = 0,
    val name: String = "",
    val email: String = "",
    val bio: String = "",
    val avatarUrl: String? = null,
    val following: String = "0",
    val followers: String = "0",
    val likes: String = "0",
    val usersSheetType: ProfileUsersSheetType? = null,
    val usersSearchQuery: String = "",
    val users: List<ProfileUserItemState> = emptyList(),
    val isUsersLoading: Boolean = false,
    val usersError: String? = null,
    val error: String? = null
)
