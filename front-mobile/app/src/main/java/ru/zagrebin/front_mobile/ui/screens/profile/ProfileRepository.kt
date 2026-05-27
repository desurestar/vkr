package ru.zagrebin.front_mobile.ui.screens.profile

import ru.zagrebin.front_mobile.data.remote.api.FeedApi
import ru.zagrebin.front_mobile.data.remote.api.UpdatePasswordRequest
import ru.zagrebin.front_mobile.data.remote.api.UpdateProfileRequest
import ru.zagrebin.front_mobile.data.local.dao.ProfileDao
import ru.zagrebin.front_mobile.data.local.entities.ProfileEntity

class ProfileRepository(private val api: FeedApi, private val profileDao: ProfileDao) {
    suspend fun getMyProfile(): ProfileData {
        return runCatching {
            val me = api.me()
            val publicProfile = api.getPublicProfile(me.id)
            ProfileData(
            id = me.id,
            name = publicProfile.displayName ?: me.displayName ?: publicProfile.username ?: me.username ?: me.email ?: "",
            email = publicProfile.email ?: me.email ?: "",
            bio = publicProfile.bio.orEmpty(),
            avatarUrl = publicProfile.avatarUrl,
            followingCount = publicProfile.following.size,
            followersCount = publicProfile.followers.size
            ).also { cacheProfile(it) }
        }.getOrElse {
            profileDao.getProfile()?.toModel() ?: throw it
        }
    }

    suspend fun updateProfile(displayName: String, bio: String, avatarUrl: String?): ProfileData {
        val updated = api.updateProfile(UpdateProfileRequest(displayName, bio, avatarUrl))
        val publicProfile = api.getPublicProfile(updated.id)
        return ProfileData(
            id = updated.id,
            name = publicProfile.displayName ?: updated.displayName ?: publicProfile.username ?: updated.username ?: updated.email ?: "",
            email = publicProfile.email ?: updated.email ?: "",
            bio = publicProfile.bio.orEmpty(),
            avatarUrl = publicProfile.avatarUrl,
            followingCount = publicProfile.following.size,
            followersCount = publicProfile.followers.size
        ).also { cacheProfile(it) }
    }

    suspend fun updatePassword(oldPassword: String, newPassword: String) {
        api.updatePassword(UpdatePasswordRequest(oldPassword, newPassword))
    }

    private suspend fun cacheProfile(profile: ProfileData) {
        profileDao.upsert(
            ProfileEntity(profile.id, profile.name, profile.email, profile.bio, profile.avatarUrl, profile.followingCount, profile.followersCount)
        )
    }
}

private fun ProfileEntity.toModel() = ProfileData(id, name, email, bio, avatarUrl, followingCount, followersCount)

data class ProfileData(
    val id: Long,
    val name: String,
    val email: String,
    val bio: String,
    val avatarUrl: String?,
    val followingCount: Int,
    val followersCount: Int
)
