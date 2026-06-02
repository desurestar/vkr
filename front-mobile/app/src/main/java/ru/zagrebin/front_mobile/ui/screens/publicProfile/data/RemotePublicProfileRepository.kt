package ru.zagrebin.front_mobile.ui.screens.publicProfile.data

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import ru.zagrebin.front_mobile.data.remote.api.FeedApi
import ru.zagrebin.front_mobile.data.remote.dto.FeedItemDto
import ru.zagrebin.front_mobile.data.remote.dto.TagDto
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagState

class RemotePublicProfileRepository(private val api: FeedApi) : PublicProfileRepository {
    override suspend fun getPublicProfile(userId: String): PublicProfileData {
        val profile = api.getPublicProfile(userId.toLong())
        val user = profile.user

        return PublicProfileData(
            userId = user.id.toString(),
            name = user.displayName?.takeIf { it.isNotBlank() } ?: user.username ?: "Пользователь",
            email = user.email ?: "",
            avatarUrl = user.avatarUrl,
            followingCount = user.following.size,
            followersCount = user.followers.size,
            isFollowing = profile.following,
            posts = profile.posts.map { it.toPostCardState() }
        )
    }

    override suspend fun setFollowState(userId: String, isFollowing: Boolean): Boolean {
        val id = userId.toLong()
        if (isFollowing) api.follow(id) else api.unfollow(id)
        return isFollowing
    }
}

private fun FeedItemDto.toPostCardState(): PostCardState = PostCardState(
    id = id,
    type = type.orEmpty().ifBlank { "RECIPE" },
    authorId = authorId.asString(),
    authorName = authorName.orEmpty(),
    authorHandle = authorHandle?.takeIf { it.startsWith("@") } ?: authorHandle?.let { "@$it" }.orEmpty(),
    authorAvatarUrl = authorAvatarUrl,
    date = formatDate(date ?: createdAt),
    title = title.orEmpty(),
    imageUrl = imageUrl.orEmpty(),
    likes = formatCount(likes),
    time = formatMinutes(time ?: cookTimeMinutes),
    calories = formatCalories(calories ?: kcalPer100),
    views = formatViews(views),
    tags = tags.map { it.toTagState() }
)

private fun TagDto.toTagState(): TagState = TagState(id = id.toInt(), title = label?.takeIf { it.isNotBlank() } ?: name)

private fun formatDate(value: String?): String {
    if (value.isNullOrBlank()) return ""
    return runCatching {
        Instant.parse(value).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }.getOrElse { value }
}

private fun formatCount(value: Any?): String {
    val number = value.asDoubleOrNull() ?: return value?.toString().orEmpty()
    return if (number >= 1_000) "${(number / 1_000.0).roundToOne()}k" else number.roundToInt().toString()
}

private fun formatMinutes(value: Any?): String {
    val number = value.asDoubleOrNull() ?: return value?.toString().orEmpty()
    return "${number.roundToInt()} мин"
}

private fun formatCalories(value: Any?): String {
    val number = value.asDoubleOrNull() ?: return value?.toString().orEmpty()
    return "${number.roundToInt()} ккал"
}

private fun formatViews(value: Any?): String = value?.let(::formatCount).orEmpty()

private fun Double.roundToOne(): String = String.format(java.util.Locale.US, "%.1f", this)

private fun Any?.asDoubleOrNull(): Double? = when (this) {
    null -> null
    is Number -> this.toDouble()
    is String -> this.toDoubleOrNull()
    else -> null
}

private fun Any?.asString(): String = when (this) {
    null -> ""
    is String -> this
    is Number -> this.toString()
    else -> this.toString()
}
