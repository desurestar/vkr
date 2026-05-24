package ru.zagrebin.front_mobile.ui.screens.profile

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
    val error: String? = null
)
