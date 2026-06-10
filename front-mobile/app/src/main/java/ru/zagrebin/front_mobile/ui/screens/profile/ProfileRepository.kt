package ru.zagrebin.front_mobile.ui.screens.profile

import ru.zagrebin.front_mobile.data.AppContainer
import ru.zagrebin.front_mobile.data.local.dao.ProfileDao
import ru.zagrebin.front_mobile.data.local.entities.ProfileEntity
import ru.zagrebin.front_mobile.data.remote.api.*
import ru.zagrebin.front_mobile.data.sync.NetworkConnectionChecker
import ru.zagrebin.front_mobile.domain.model.ProfileData

private const val PROFILE_USERS_PAGE_SIZE = 50

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

    suspend fun getFollowingUsers(userId: Long, query: String): List<UserProfileDto> = api.getProfileFollowing(
        userId = userId,
        query = query.normalizeUserQuery(),
        page = 0,
        size = PROFILE_USERS_PAGE_SIZE
    )

    suspend fun getFollowerUsers(userId: Long, query: String): List<UserProfileDto> = api.getProfileFollowers(
        userId = userId,
        query = query.normalizeUserQuery(),
        page = 0,
        size = PROFILE_USERS_PAGE_SIZE
    )

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
                avatarUrl = AppContainer.toRelativeMediaPath(profile.avatarUrl ?: old?.avatarUrl),
                followingCount = profile.followingCount,
                followersCount = profile.followersCount,
                totalLikes = profile.totalLikes
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
            avatarUrl = AppContainer.toRelativeMediaPath(avatarUrl),
            followingCount = followingCount,
            followersCount = followersCount,
            totalLikes = totalLikes
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
            avatarUrl = AppContainer.toRelativeMediaPath(user?.avatarUrl),

            followingCount = user?.following?.size ?: 0,
            followersCount = user?.followers?.size ?: 0,
            totalLikes = user?.totalLikes ?: public?.posts?.sumOf { post -> post.likes.toIntCount() } ?: 0
        )
    }
}

private fun String.normalizeUserQuery(): String = trim().removePrefix("@").trim()

private fun Any?.toIntCount(): Int = when (this) {
    is Number -> toInt()
    is String -> toIntOrNull() ?: 0
    else -> 0
}
