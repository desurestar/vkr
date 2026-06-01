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

interface FeedApi {
    @GET("api/v1/feed/recipes")
    suspend fun getRecipesFeed(@Query("q") query: String? = null): List<FeedItemDto>

    @GET("api/v1/feed/articles")
    suspend fun getArticlesFeed(@Query("q") query: String? = null): List<FeedItemDto>

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

    @POST("api/v1/posts/{id}/comments") suspend fun addComment(@Path("id") postId: Int, @Body request: CommentRequest)
    @GET("api/v1/posts/{id}/comments") suspend fun getComments(@Path("id") postId: Int): List<CommentDto>

    @POST("api/v1/posts/{id}/likes") suspend fun like(@Path("id") postId: Int)
    @DELETE("api/v1/posts/{id}/likes") suspend fun unlike(@Path("id") postId: Int)

    @POST("api/v1/profile/{userId}/follow") suspend fun follow(@Path("userId") userId: Long)
    @DELETE("api/v1/profile/{userId}/follow") suspend fun unfollow(@Path("userId") userId: Long)
    @GET("api/v1/profile/{userId}") suspend fun getPublicProfile(@Path("userId") userId: Long): UserProfileDto

    @GET("api/v1/search") suspend fun search(@Query("query") query: String, @Query("type") type: String? = null, @Query("tag") tag: String? = null): SearchResponse
    @POST("api/v1/recipes") suspend fun createRecipe(@Body request: CreateRecipeRequest): RecipeDetailsDto
    @POST("api/v1/articles") suspend fun createArticle(@Body request: CreateArticleRequest): ArticleDetailsDto
    @GET("api/v1/tags") suspend fun getTags(@Query("q") query: String? = null): List<TagDto>
    @Multipart
    @POST("api/v1/media") suspend fun uploadMedia(@Part file: MultipartBody.Part): MediaUploadResponse
}

data class AuthRequest(val email: String, val password: String, val username: String? = null)
data class SessionUserDto(val id: Long, val username: String? = null, val displayName: String? = null, val email: String? = null)
data class UpdateProfileRequest(val displayName: String, val bio: String, val avatarUrl: String?)
data class UpdatePasswordRequest(val oldPassword: String, val newPassword: String)
data class CommentRequest(val text: String)
data class CommentDto(val id: Long, val authorId: Long, val text: String, val createdAt: String)
data class SearchResponse(val posts: List<ServerPostDto> = emptyList(), val users: List<SessionUserDto> = emptyList())
data class MediaUploadResponse(val url: String)


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
    val tags: List<String>,
    val ingredients: List<CreateRecipeIngredient>,
    val steps: List<CreateRecipeStep>
)

data class CreateRecipeIngredient(val name: String, val amount: Double, val unit: String)
data class CreateRecipeStep(val number: Int, val description: String, val imageUrl: String? = null)

data class CreateArticleRequest(
    val title: String,
    val summary: String? = null,
    val content: String,
    val imageUrl: String? = null,
    val tags: List<String> = emptyList()
)
