package ru.zagrebin.front_mobile.ui.screens.profile

import ru.zagrebin.front_mobile.domain.model.ProfileData

data class ProfileLoadResult(
    val profile: ProfileData,
    val isFromCache: Boolean
)