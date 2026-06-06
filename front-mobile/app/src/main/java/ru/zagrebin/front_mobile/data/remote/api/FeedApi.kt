package ru.zagrebin.front_mobile.data.remote.api

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import ru.zagrebin.front_mobile.data.remote.dto.ArticleDetailsDto
import ru.zagrebin.front_mobile.data.remote.dto.FeedItemDto
import ru.zagrebin.front_mobile.data.remote.dto.RecipeDetailsDto
import ru.zagrebin.front_mobile.data.remote.dto.ServerPostDto
import ru.zagrebin.front_mobile.data.remote.dto.TagDto
import ru.zagrebin.front_mobile.data.remote.dto.CommentDto

interface FeedApi {
    @GET("api/v1/feed/recipes")
    suspend fun getRecipesFeed(
        @Query("q") query: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("minTime") minTime: Int? = null,
        @Query("maxTime") maxTime: Int? = null,
        @Query("minCalories") minCalories: Double? = null,
        @Query("maxCalories") maxCalories: Double? = null,
        @Query("minProteins") minProteins: Double? = null,
        @Query("maxProteins") maxProteins: Double? = null,
        @Query("minFats") minFats: Double? = null,
        @Query("maxFats") maxFats: Double? = null,
        @Query("minCarbs") minCarbs: Double? = null,
        @Query("maxCarbs") maxCarbs: Double? = null,
        @Query("tags") tags: List<String>? = null
    ): List<FeedItemDto>

    @GET("api/v1/feed/articles")
    suspend fun getArticlesFeed(
        @Query("q") query: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("tags") tags: List<String>? = null
    ): List<FeedItemDto>

    @GET("api/v1/recipes/{id}")
    suspend fun getRecipeDetails(@Path("id") id: Int): RecipeDetailsDto

    @GET("api/v1/articles/{id}")
    suspend fun getArticleDetails(@Path("id") id: Int): ArticleDetailsDto

    @POST("api/v1/auth/register") suspend fun register(@Body request: AuthRequest): SessionUserDto
    @POST("api/v1/auth/login") suspend fun login(@Body request: AuthRequest): SessionUserDto
    @POST("api/v1/auth/logout") suspend fun logout()
    @GET("api/v1/auth/me") suspend fun me(): SessionUserDto

    @PATCH("api/v1/profile") suspend fun updateProfile(@Body request: UpdateProfileRequest): SessionUserDto
    @PATCH("api/v1/profile/password") suspend fun updatePassword(@Body request: UpdatePasswordRequest)

    @POST("api/v1/posts/{id}/comments") suspend fun addComment(@Path("id") postId: Int, @Body request: CommentRequest): CommentDto
    @GET("api/v1/posts/{id}/comments") suspend fun getComments(@Path("id") postId: Int): List<CommentDto>
    @DELETE("api/v1/comments/{id}") suspend fun deleteComment(@Path("id") commentId: Long)

    @POST("api/v1/posts/{id}/likes") suspend fun like(@Path("id") postId: Int): FeedItemDto
    @DELETE("api/v1/posts/{id}/likes") suspend fun unlike(@Path("id") postId: Int): FeedItemDto

    @POST("api/v1/posts/{id}/views") suspend fun recordView(@Path("id") postId: Int, @Body request: PostViewRequest): FeedItemDto

    @POST("api/v1/profile/{userId}/follow") suspend fun follow(@Path("userId") userId: Long)
    @DELETE("api/v1/profile/{userId}/follow") suspend fun unfollow(@Path("userId") userId: Long)
    @GET("api/v1/profile/{userId}") suspend fun getPublicProfile(@Path("userId") userId: Long, @Query("q") query: String? = null, @Query("page") page: Int? = null, @Query("size") size: Int? = null): PublicProfileDto

    @GET("api/v1/search") suspend fun search(@Query("query") query: String, @Query("type") type: String? = null, @Query("tag") tag: String? = null, @Query("page") page: Int? = null, @Query("size") size: Int? = null): SearchResponse
    @GET("api/v1/search/users") suspend fun searchUsers(@Query("query") query: String, @Query("page") page: Int? = null, @Query("size") size: Int? = null): List<UserProfileDto>
    @POST("api/v1/recipes") suspend fun createRecipe(@Body request: CreateRecipeRequest): RecipeDetailsDto
    @POST("api/v1/articles") suspend fun createArticle(@Body request: CreateArticleRequest): ArticleDetailsDto
    @GET("api/v1/drafts") suspend fun getDrafts(): List<FeedItemDto>
    @DELETE("api/v1/drafts/{id}") suspend fun deleteDraft(@Path("id") id: Int)

    @GET("api/v1/profile/shopping-list") suspend fun getShoppingLists(): List<ShoppingListDto>
    @POST("api/v1/profile/shopping-list") suspend fun createShoppingList(@Body request: Map<String, String>): ShoppingListDto
    @PATCH("api/v1/profile/shopping-list/{listId}") suspend fun updateShoppingList(@Path("listId") listId: Long, @Body request: Map<String, String>): ShoppingListDto
    @DELETE("api/v1/profile/shopping-list/{listId}") suspend fun deleteShoppingList(@Path("listId") listId: Long)
    @POST("api/v1/profile/shopping-list/{listId}/items") suspend fun addShoppingItem(@Path("listId") listId: Long, @Body request: ShoppingItemRequest): ShoppingItemDto
    @PATCH("api/v1/profile/shopping-list/items/{itemId}") suspend fun updateShoppingItem(@Path("itemId") itemId: Long, @Body request: ShoppingItemRequest): ShoppingItemDto
    @DELETE("api/v1/profile/shopping-list/items/{itemId}") suspend fun deleteShoppingItem(@Path("itemId") itemId: Long)
    @POST("api/v1/recipes/{id}/shopping-list") suspend fun addRecipeToShoppingList(@Path("id") id: Int)

    @GET("api/v1/statistics") suspend fun getStatistics(@Query("month") month: String? = null): StatisticsResponseDto
    @PATCH("api/v1/statistics/settings") suspend fun updateStatisticsSettings(@Body request: StatisticsSettingsRequest): StatisticsSettingsDto
    @POST("api/v1/statistics/water") suspend fun addStatisticsWater(@Body request: AddWaterRequest): StatisticsDayDto
    @POST("api/v1/statistics/meals") suspend fun addStatisticsMeal(@Body request: AddMealRequest): StatisticsMealEntryDto
    @POST("api/v1/statistics/recipes/{recipeId}/meals") suspend fun addRecipeStatisticsMeal(@Path("recipeId") recipeId: Int, @Body request: AddRecipeMealRequest): StatisticsMealEntryDto

    @GET("api/v1/tags") suspend fun getTags(@Query("q") query: String? = null): List<TagDto>
    @Multipart
    @POST("api/v1/media") suspend fun uploadMedia(@Part file: MultipartBody.Part): MediaUploadResponse
}

data class AuthRequest(val email: String, val password: String, val username: String? = null)
data class SessionUserDto(val id: Long, val username: String? = null, val displayName: String? = null, val email: String? = null)
data class UpdateProfileRequest(val displayName: String, val bio: String, val avatarUrl: String?)
data class UpdatePasswordRequest(val oldPassword: String, val newPassword: String)
data class CommentRequest(val text: String, val parentId: Long? = null)
data class PostViewRequest(val durationSeconds: Int)
data class SearchResponse(val posts: List<ServerPostDto> = emptyList(), val users: List<UserProfileDto> = emptyList())
data class MediaUploadResponse(val url: String)


data class PublicProfileDto(
    val user: UserProfileDto,
    val following: Boolean = false,
    val posts: List<FeedItemDto> = emptyList()
)

data class UserProfileDto(
    val id: Long,
    val email: String? = null,
    val username: String? = null,
    val displayName: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val following: Set<Long> = emptySet(),
    val followers: Set<Long> = emptySet()
)

data class CreateRecipeRequest(
    val title: String,
    val summary: String,
    val content: String,
    val imageUrl: String? = null,
    val cookTimeMinutes: Int,
    val proteinsPer100: Double,
    val fatsPer100: Double,
    val carbsPer100: Double,
    val kcalPer100: Double,
    val status: String = "PUBLISHED",
    val tags: List<String>,
    val ingredients: List<CreateRecipeIngredient>,
    val steps: List<CreateRecipeStep>
)

data class CreateRecipeIngredient(val name: String, val amount: Double, val unit: String)
data class CreateRecipeStep(val number: Int, val description: String, val imageUrl: String? = null)


data class CreateArticleRequest(
    val title: String,
    val summary: String,
    val content: String,
    val imageUrl: String? = null,
    val status: String = "PUBLISHED",
    val tags: List<String>
)


data class ShoppingListDto(val id: Long, val name: String, val items: List<ShoppingItemDto> = emptyList())
data class ShoppingItemDto(val id: Long, val name: String, val amount: String = "", val checked: Boolean = false)
data class ShoppingItemRequest(val name: String? = null, val amount: String? = null, val checked: Boolean? = null)

data class StatisticsSettingsDto(
    val retentionMonths: Int = 3,
    val goalKcal: Int = 2000,
    val waterGoalMl: Int = 1500,
    val proteinGoalGrams: Int = 90,
    val fatGoalGrams: Int = 70,
    val carbsGoalGrams: Int = 250
)

data class StatisticsMealEntryDto(
    val id: Long,
    val name: String,
    val amountLabel: String,
    val timeLabel: String,
    val kcal: Int,
    val proteins: Float,
    val fats: Float,
    val carbs: Float
)

data class StatisticsDayDto(
    val date: String,
    val goalKcal: Int,
    val waterGoalMl: Int,
    val waterConsumedMl: Int,
    val breakfast: List<StatisticsMealEntryDto> = emptyList(),
    val lunch: List<StatisticsMealEntryDto> = emptyList(),
    val dinner: List<StatisticsMealEntryDto> = emptyList(),
    val snack: List<StatisticsMealEntryDto> = emptyList()
)

data class StatisticsResponseDto(val settings: StatisticsSettingsDto, val days: List<StatisticsDayDto>)
data class StatisticsSettingsRequest(
    val retentionMonths: Int? = null,
    val goalKcal: Int? = null,
    val waterGoalMl: Int? = null,
    val proteinGoalGrams: Int? = null,
    val fatGoalGrams: Int? = null,
    val carbsGoalGrams: Int? = null
)
data class AddWaterRequest(val date: String, val amountMl: Int)
data class AddMealRequest(
    val date: String,
    val type: String,
    val name: String,
    val amountLabel: String,
    val timeLabel: String,
    val kcal: Int,
    val proteins: Float,
    val fats: Float,
    val carbs: Float
)

data class AddRecipeMealRequest(
    val date: String,
    val type: String,
    val portionGrams: Int,
    val liquid: Boolean,
    val timeLabel: String
)
