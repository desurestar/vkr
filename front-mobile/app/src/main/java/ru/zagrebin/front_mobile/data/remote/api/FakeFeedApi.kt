package ru.zagrebin.front_mobile.data.remote.api

import kotlinx.coroutines.delay
import ru.zagrebin.front_mobile.data.remote.dto.*

class FakeFeedApi : FeedApi {
    override suspend fun getRecipesFeed(query: String?): List<FeedItemDto> { delay(200); return sample(0, "Рецепт") }
    override suspend fun getArticlesFeed(query: String?): List<FeedItemDto> { delay(200); return sample(100, "Статья") }
    override suspend fun getRecipeDetails(id: Int): RecipeDetailsDto { delay(100); return sampleRecipeDetails(id) }
    override suspend fun getArticleDetails(id: Int): ArticleDetailsDto { delay(100); return sampleArticleDetails(id) }
    override suspend fun register(request: AuthRequest): SessionUserDto = SessionUserDto(1, request.username ?: "Demo", request.email)
    override suspend fun login(request: AuthRequest): SessionUserDto = SessionUserDto(1, "Demo", request.email)
    override suspend fun logout() {}
    override suspend fun me(): SessionUserDto = SessionUserDto(1, "Demo", "demo@example.com")
    override suspend fun updateProfile(request: UpdateProfileRequest): SessionUserDto = SessionUserDto(1, request.displayName, "demo@example.com")
    override suspend fun updatePassword(request: UpdatePasswordRequest) {}
    override suspend fun addComment(postId: Int, request: CommentRequest) {}
    override suspend fun getComments(postId: Int): List<CommentDto> = emptyList()
    override suspend fun like(postId: Int) {}
    override suspend fun unlike(postId: Int) {}
    override suspend fun follow(userId: Long) {}
    override suspend fun unfollow(userId: Long) {}
    override suspend fun getPublicProfile(userId: Long): UserProfileDto {
        TODO("Not yet implemented")
    }

    override suspend fun search(query: String, type: String?, tag: String?): SearchResponse = SearchResponse()

    private fun sample(offset: Int, prefix: String): List<FeedItemDto> = List(10) { index ->
        FeedItemDto(offset + index, if (index % 2 == 0) "42" else "77", "Дмитрий Загребин", "@Dima123", "22.05.2026", "$prefix #${index + 1}", "", "${30 + index}k", "${20 + index} мин", "${200 + index * 5} ккал", "${50 + index}k")
    }
    private fun sampleRecipeDetails(id: Int) = RecipeDetailsDto(id, "42", "Дмитрий Загребин", "@Dima123", "22.05.2026", "Рецепт #$id", "", "38.6k", "35 мин", "250 ккал", "53.7k", id % 2 == 0, 9.5f, 6.2f, 18.4f, 145, listOf(RecipeTagDto(1, "#ужин")), listOf(RecipeIngredientDto("Творог - 500 г")), listOf(RecipeStepDto(1, "Шаг 1", "Описание", null)))
    private fun sampleArticleDetails(id: Int) = ArticleDetailsDto(id, "77", "Анна Калинина", "@anna", "20.05.2026", "Статья #$id", "", "12.4k", "18.2k", "Контент", id % 2 == 1)
}
