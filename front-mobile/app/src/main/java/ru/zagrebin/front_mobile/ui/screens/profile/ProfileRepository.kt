package ru.zagrebin.front_mobile.ui.screens.profile

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.zagrebin.front_mobile.data.local.dao.ProfileDao
import ru.zagrebin.front_mobile.data.local.entities.ProfileEntity
import ru.zagrebin.front_mobile.data.remote.api.FeedApi
import ru.zagrebin.front_mobile.data.remote.api.SessionUserDto
import ru.zagrebin.front_mobile.data.remote.api.UpdatePasswordRequest
import ru.zagrebin.front_mobile.data.remote.api.UpdateProfileRequest
import ru.zagrebin.front_mobile.data.remote.api.UserProfileDto
import ru.zagrebin.front_mobile.data.repository.RefreshResult
import ru.zagrebin.front_mobile.data.sync.NetworkConnectionChecker

class ProfileRepository(
    private val api: FeedApi,
    private val profileDao: ProfileDao,
    private val networkConnectionChecker: NetworkConnectionChecker
) {
    fun observeMyProfile(): Flow<ProfileData?> = profileDao.observeProfile().map { it?.toModel() }

    suspend fun cacheAuthenticatedProfile(user: SessionUserDto): ProfileData {
        val publicProfile = runCatching { api.getPublicProfile(user.id) }.getOrNull()
        return user.toProfileData(publicProfile).also { cacheProfile(it) }
    }

    suspend fun clearProfile() {
        profileDao.clear()
    }

    suspend fun refreshMyProfile(): RefreshResult {
        if (!networkConnectionChecker.isNetworkAvailable()) return RefreshResult.Fallback
        return runCatching {
            fetchRemoteProfile().also { cacheProfile(it) }
            RefreshResult.Success
        }.getOrElse { RefreshResult.Fallback }
    }

    suspend fun getMyProfile(): ProfileData {
        val result = refreshMyProfile()
        val cachedProfile = profileDao.getProfile()?.toModel()
        if (cachedProfile != null) return cachedProfile
        if (result == RefreshResult.Fallback) error("Сервер недоступен, а офлайн-кеш профиля пуст")
        return profileDao.getProfile()?.toModel() ?: error("Профиль не найден")
    }

    suspend fun updateProfile(displayName: String, bio: String, avatarUrl: String?): ProfileData {
        val updated = api.updateProfile(UpdateProfileRequest(displayName, bio, avatarUrl))
        val publicProfile = api.getPublicProfile(updated.id)
        return updated.toProfileData(publicProfile).also { cacheProfile(it) }
    }

    suspend fun updatePassword(oldPassword: String, newPassword: String) {
        api.updatePassword(UpdatePasswordRequest(oldPassword, newPassword))
    }

    private suspend fun fetchRemoteProfile(): ProfileData {
        val me = api.me()
        val publicProfile = runCatching { api.getPublicProfile(me.id) }.getOrNull()
        return me.toProfileData(publicProfile)
    }

    private suspend fun cacheProfile(profile: ProfileData) {
        profileDao.replace(
            ProfileEntity(profile.id, profile.name, profile.email, profile.bio, profile.avatarUrl, profile.followingCount, profile.followersCount)
        )
    }
}

private fun SessionUserDto.toProfileData(publicProfile: UserProfileDto?): ProfileData {
    val publicName = publicProfile?.displayName ?: publicProfile?.username
    val publicEmail = publicProfile?.email
    return ProfileData(
        id = id,
        name = publicName ?: displayName ?: username ?: email ?: "",
        email = publicEmail ?: email ?: "",
        bio = publicProfile?.bio.orEmpty(),
        avatarUrl = publicProfile?.avatarUrl,
        followingCount = publicProfile?.following?.size ?: 0,
        followersCount = publicProfile?.followers?.size ?: 0
    )
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
