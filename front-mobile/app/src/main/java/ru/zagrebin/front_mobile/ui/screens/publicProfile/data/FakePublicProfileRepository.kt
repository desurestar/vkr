package ru.zagrebin.front_mobile.ui.screens.publicProfile.data

import kotlinx.coroutines.delay
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagState

class FakePublicProfileRepository : PublicProfileRepository {

    private val follows = mutableMapOf<String, Boolean>()

    override suspend fun getPublicProfile(userId: String): PublicProfileData {
        delay(120)

        val posts = List(4) { index ->
            PostCardState(
                id = 10_000 + index,
                authorId = userId,
                authorName = "Дмитрий Загребин",
                authorHandle = "@Dima123",
                date = "24.03.2026",
                title = "Токпокки (Tteokbokki) - классический рецепт #${index + 1}",
                imageUrl = "https://img.freepik.com/premium-photo/korean-street-food-tteokbokki_102375-5144.jpg",
                likes = "38.6k",
                time = "35 мин",
                calories = "250 ккал",
                views = "53.7k",
                tags = listOf(
                    TagState(1, "#tteokbokki"),
                    TagState(2, "#корея"),
                    TagState(3, "#острое"),
                    TagState(4, "#streetfood")
                )
            )
        }

        return PublicProfileData(
            userId = userId,
            name = "Дмитрий Загребин",
            handle = "@Dima123",
            avatarUrl = null,
            followingCount = 828,
            followersCount = 72_900,
            isFollowing = follows[userId] ?: false,
            isOwnProfile = false,
            posts = posts
        )
    }

    override suspend fun setFollowState(userId: String, isFollowing: Boolean): Boolean {
        delay(80)
        follows[userId] = isFollowing
        return isFollowing
    }
}
