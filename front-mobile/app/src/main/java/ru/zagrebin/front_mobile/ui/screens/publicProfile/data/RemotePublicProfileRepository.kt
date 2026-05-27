package ru.zagrebin.front_mobile.ui.screens.publicProfile.data

import ru.zagrebin.front_mobile.data.remote.api.FeedApi
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState

class RemotePublicProfileRepository(private val api: FeedApi) : PublicProfileRepository {
    override suspend fun getPublicProfile(userId: String): PublicProfileData {
        val id = userId.toLong()
        val profile = api.getPublicProfile(id)
        val posts = (api.getRecipesFeed() + api.getArticlesFeed())
            .filter { it.authorId.asLongOrNull() == id }
            .sortedByDescending { it.id }
            .map { item ->
                PostCardState(
                    id = item.id,
                    authorId = item.authorId.asString(),
                    authorName = item.authorName ?: "",
                    authorHandle = item.authorHandle ?: "",
                    date = item.date ?: "",
                    title = item.title ?: "",
                    imageUrl = item.imageUrl ?: "",
                    likes = item.likes?.toString() ?: "",
                    time = item.time?.toString() ?: "",
                    calories = item.calories?.toString() ?: "",
                    views = item.views?.toString() ?: ""
                )
            }

        return PublicProfileData(
            userId = profile.id.toString(),
            name = profile.displayName ?: "",
            email = profile.email ?: "",
            avatarUrl = profile.avatarUrl,
            followingCount = profile.following.size,
            followersCount = profile.followers.size,
            isFollowing = false,
            posts = posts
        )
    }

    override suspend fun setFollowState(userId: String, isFollowing: Boolean): Boolean {
        val id = userId.toLong()
        if (isFollowing) api.follow(id) else api.unfollow(id)
        return isFollowing
    }
}

private fun Any?.asLongOrNull(): Long? = when (this) {
    null -> null
    is Number -> this.toLong()
    is String -> this.toLongOrNull()
    else -> null
}

private fun Any?.asString(): String = when (this) {
    null -> ""
    is String -> this
    is Number -> this.toString()
    else -> this.toString()
}
