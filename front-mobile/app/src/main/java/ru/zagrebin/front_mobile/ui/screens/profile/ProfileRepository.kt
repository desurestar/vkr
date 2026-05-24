package ru.zagrebin.front_mobile.ui.screens.profile

import ru.zagrebin.front_mobile.data.remote.api.FeedApi
import ru.zagrebin.front_mobile.data.remote.api.UpdatePasswordRequest
import ru.zagrebin.front_mobile.data.remote.api.UpdateProfileRequest

class ProfileRepository(private val api: FeedApi) {
    suspend fun getMyProfile(): ProfileData {
        val me = api.me()
        val publicProfile = api.getPublicProfile(me.id)
        return ProfileData(
            id = me.id,
            name = publicProfile.displayName ?: me.displayName ?: me.email ?: "",
            email = publicProfile.email ?: me.email ?: "",
            bio = publicProfile.bio.orEmpty(),
            avatarUrl = publicProfile.avatarUrl,
            followingCount = publicProfile.following.size,
            followersCount = publicProfile.followers.size
        )
    }

    suspend fun updateProfile(displayName: String, bio: String, avatarUrl: String?): ProfileData {
        val updated = api.updateProfile(UpdateProfileRequest(displayName, bio, avatarUrl))
        val publicProfile = api.getPublicProfile(updated.id)
        return ProfileData(
            id = updated.id,
            name = publicProfile.displayName ?: updated.displayName ?: updated.email ?: "",
            email = publicProfile.email ?: updated.email ?: "",
            bio = publicProfile.bio.orEmpty(),
            avatarUrl = publicProfile.avatarUrl,
            followingCount = publicProfile.following.size,
            followersCount = publicProfile.followers.size
        )
    }

    suspend fun updatePassword(oldPassword: String, newPassword: String) {
        api.updatePassword(UpdatePasswordRequest(oldPassword, newPassword))
    }
}

data class ProfileData(
    val id: Long,
    val name: String,
    val email: String,
    val bio: String,
    val avatarUrl: String?,
    val followingCount: Int,
    val followersCount: Int
)
