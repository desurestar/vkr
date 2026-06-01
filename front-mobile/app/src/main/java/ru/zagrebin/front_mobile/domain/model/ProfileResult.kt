package ru.zagrebin.front_mobile.domain.model

data class ProfileResult(
    val profile: ProfileData,
    val isFromCache: Boolean
)