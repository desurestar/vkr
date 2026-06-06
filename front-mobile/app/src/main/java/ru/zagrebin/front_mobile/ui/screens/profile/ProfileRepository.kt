package ru.zagrebin.front_mobile.ui.screens.profile

import ru.zagrebin.front_mobile.data.local.dao.ProfileDao
import ru.zagrebin.front_mobile.data.local.entities.ProfileEntity
import ru.zagrebin.front_mobile.data.remote.api.*
import ru.zagrebin.front_mobile.data.sync.NetworkConnectionChecker
import ru.zagrebin.front_mobile.domain.model.ProfileData

class ProfileRepository(
    private val api: FeedApi,
    private val profileDao: ProfileDao,
    private val networkConnectionChecker: NetworkConnectionChecker
) {

    suspend fun cacheAuthenticatedProfile(user: SessionUserDto): ProfileData {
        val public = runCatching {
            api.getPublicProfile(user.id)
        }.getOrNull()

        val profile = user.toProfileData(public)

        cacheProfile(profile)

        return profile
    }

    suspend fun getMyProfile(): ProfileLoadResult {

        if (networkConnectionChecker.isNetworkAvailable()) {
            runCatching { fetchRemoteProfile() }
                .onSuccess { profile ->
                    cacheProfile(profile)
                    return ProfileLoadResult(profile, isFromCache = false)
                }
        }

        val cached = profileDao.getProfile()?.toModel()
            ?: error("Сервер недоступен и кэш пуст")

        return ProfileLoadResult(cached, isFromCache = true)
    }



    suspend fun updateProfile(
        displayName: String,
        bio: String,
        avatarUrl: String?
    ): ProfileData {

        if (!networkConnectionChecker.isNetworkAvailable()) {
            error("Нет интернета: обновление невозможно")
        }

        val updated = api.updateProfile(
            UpdateProfileRequest(displayName, bio, avatarUrl)
        )

        val public = api.getPublicProfile(updated.id)

        val profile = updated.toProfileData(public)

        cacheProfile(profile)

        return profile
    }

    suspend fun clearProfile() {
        profileDao.clear()
    }

    private suspend fun fetchRemoteProfile(): ProfileData {
        val me = api.me()

        val public = runCatching {
            api.getPublicProfile(me.id)
        }.getOrNull()

        return me.toProfileData(public)
    }

    private suspend fun cacheProfile(profile: ProfileData) {
        val old = profileDao.getProfile()

        profileDao.replace(
            ProfileEntity(
                id = profile.id,
                name = profile.name,
                email = profile.email,
                bio = profile.bio,
                avatarUrl = profile.avatarUrl ?: old?.avatarUrl,
                followingCount = profile.followingCount,
                followersCount = profile.followersCount
            )
        )
    }

    // =========================
    // MAPPERS
    // =========================
    private fun ProfileEntity.toModel(): ProfileData {
        return ProfileData(
            id = id,
            name = name,
            email = email,
            bio = bio,
            avatarUrl = avatarUrl,
            followingCount = followingCount,
            followersCount = followersCount
        )
    }

    private fun SessionUserDto.toProfileData(public: PublicProfileDto?): ProfileData {
        val user = public?.user
        return ProfileData(
            id = id,
            name = user?.displayName ?: username ?: email ?: "",
            email = user?.email ?: email ?: "",
            bio = user?.bio.orEmpty(),

            // 🔥 ONLY SERVER VALUE
            avatarUrl = user?.avatarUrl,

            followingCount = user?.following?.size ?: 0,
            followersCount = user?.followers?.size ?: 0
        )
    }
}
