package ru.zagrebin.front_mobile.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import ru.zagrebin.front_mobile.data.remote.dto.ArticleDetailsDto
import ru.zagrebin.front_mobile.data.remote.dto.FeedItemDto

import ru.zagrebin.front_mobile.data.remote.dto.RecipeDetailsDto

interface FeedApi {
    @GET("recipes")
    suspend fun getRecipesFeed(): List<FeedItemDto>

    @GET("articles")
    suspend fun getArticlesFeed(): List<FeedItemDto>

    @GET("recipes/{id}")
    suspend fun getRecipeDetails(@Path("id") id: Int): RecipeDetailsDto

    @GET("articles/{id}")
    suspend fun getArticleDetails(@Path("id") id: Int): ArticleDetailsDto
}