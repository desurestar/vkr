// package ru.zagrebin.front_mobile.data.remote.api
//
// import kotlinx.coroutines.delay
// import okhttp3.MultipartBody
// import ru.zagrebin.front_mobile.data.remote.dto.*
//
// class FakeFeedApi : FeedApi {
//     override suspend fun getRecipesFeed(query: String?): List<FeedItemDto> { delay(200); return sample(0, "Рецепт") }
//     override suspend fun getArticlesFeed(query: String?): List<FeedItemDto> { delay(200); return sample(100, "Статья") }
//     override suspend fun getRecipeDetails(id: Int): RecipeDetailsDto { delay(100); return sampleRecipeDetails(id) }
//     override suspend fun getArticleDetails(id: Int): ArticleDetailsDto { delay(100); return sampleArticleDetails(id) }
//     override suspend fun register(request: AuthRequest): SessionUserDto = SessionUserDto(1, request.username ?: "Demo", request.email)
//     override suspend fun login(request: AuthRequest): SessionUserDto = SessionUserDto(1, "Demo", request.email)
//     override suspend fun logout() {}
//     override suspend fun me(): SessionUserDto = SessionUserDto(1, "Demo", "demo@example.com")
//     override suspend fun updateProfile(request: UpdateProfileRequest): SessionUserDto = SessionUserDto(1, request.displayName, "demo@example.com")
//     override suspend fun updatePassword(request: UpdatePasswordRequest) {}
//     override suspend fun addComment(postId: Int, request: CommentRequest) {}
//     override suspend fun getComments(postId: Int): List<CommentDto> = emptyList()
//     override suspend fun like(postId: Int) {}
//     override suspend fun unlike(postId: Int) {}
//     override suspend fun follow(userId: Long) {}
//     override suspend fun unfollow(userId: Long) {}
//     override suspend fun getPublicProfile(userId: Long): UserProfileDto {
//         TODO("Not yet implemented")
//     }
//
//     override suspend fun search(query: String, type: String?, tag: String?): SearchResponse = SearchResponse()
//
//     override suspend fun createRecipe(request: CreateRecipeRequest): RecipeDetailsDto {
//         delay(150)
//         return RecipeDetailsDto(
//             id = 999,
//             authorId = 1,
//             authorName = "Дмитрий Загребин",
//             authorHandle = "@Dima123",
//             createdAt = "2026-05-27T00:00:00Z",
//             title = request.title,
//             imageUrl = request.imageUrl,
//             likes = 0,
//             cookTimeMinutes = request.cookTimeMinutes,
//             proteinsPer100 = request.proteinsPer100,
//             fatsPer100 = request.fatsPer100,
//             carbsPer100 = request.carbsPer100,
//             kcalPer100 = request.kcalPer100,
//             tags = request.tags.mapIndexed { index, tag -> RecipeTagDto(index + 1, tag) },
//             ingredients = request.ingredients.map { ingredient ->
//                 RecipeIngredientDto(name = ingredient.name, amount = ingredient.amount, unit = ingredient.unit)
//             },
//             steps = request.steps.map { step ->
//                 RecipeStepDto(number = step.number, description = step.description, imageUrl = step.imageUrl)
//             }
//         )
//     }
//
//     override suspend fun getTags(query: String?): List<TagDto> {
//         delay(80)
//         val tags = listOf(
//             TagDto(1, "Завтрак", "Завтрак", "#B57A1D"),
//             TagDto(2, "Обед", "Обед", "#B57A1D"),
//             TagDto(3, "Ужин", "Ужин", "#B57A1D"),
//             TagDto(4, "ПП", "ПП", "#B57A1D"),
//             TagDto(5, "Веган", "Веган", "#B57A1D")
//         )
//         val queryValue = query?.trim().orEmpty()
//         return if (queryValue.isBlank()) {
//             tags
//         } else {
//             tags.filter { it.name.contains(queryValue, ignoreCase = true) }
//         }
//     }
//
//     override suspend fun uploadMedia(file: MultipartBody.Part): MediaUploadResponse {
//         delay(100)
//         return MediaUploadResponse(
//             url = "https://example.com/fake-image.jpg"
//         )
//     }
//
//     private fun sample(offset: Int, prefix: String): List<FeedItemDto> = List(10) { index ->
//         FeedItemDto(
//             id = offset + index,
//             authorId = if (index % 2 == 0) "42" else "77",
//             authorName = "Дмитрий Загребин",
//             authorHandle = "@Dima123",
//             date = "22.05.2026",
//             title = "$prefix #${index + 1}",
//             imageUrl = "",
//             likes = "${30 + index}k",
//             time = "${20 + index} мин",
//             calories = "${200 + index * 5} ккал",
//             views = "${50 + index}k"
//         )
//     }
//     private fun sampleRecipeDetails(id: Int) = RecipeDetailsDto(
//         id = id,
//         authorId = "42",
//         authorName = "Дмитрий Загребин",
//         authorHandle = "@Dima123",
//         date = "22.05.2026",
//         title = "Рецепт #$id",
//         imageUrl = "",
//         likes = "38.6k",
//         time = "35 мин",
//         calories = "250 ккал",
//         views = "53.7k",
//         isSaved = id % 2 == 0,
//         proteinsPer100 = 9.5,
//         fatsPer100 = 6.2,
//         carbsPer100 = 18.4,
//         kcalPer100 = 145.0,
//         tags = listOf(RecipeTagDto(1, "#ужин")),
//         ingredients = listOf(RecipeIngredientDto(text = "Творог - 500 г")),
//         steps = listOf(RecipeStepDto(id = 1, title = "Шаг 1", description = "Описание", imageUrl = null))
//     )
//     private fun sampleArticleDetails(id: Int) = ArticleDetailsDto(id, "77", "Анна Калинина", "@anna", "20.05.2026", "Статья #$id", "", "12.4k", "18.2k", "Контент", id % 2 == 1)
// }
