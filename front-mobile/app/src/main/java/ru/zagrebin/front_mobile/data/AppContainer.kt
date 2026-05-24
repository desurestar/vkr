package ru.zagrebin.front_mobile.data

import android.content.Context
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import ru.zagrebin.front_mobile.data.local.AppDatabase
import ru.zagrebin.front_mobile.data.remote.api.FeedApi
import ru.zagrebin.front_mobile.data.repository.FeedRepository
import ru.zagrebin.front_mobile.domain.usecase.ObserveArticleDetailsUseCase
import ru.zagrebin.front_mobile.domain.usecase.ObserveArticlesFeedUseCase
import ru.zagrebin.front_mobile.domain.usecase.ObserveRecipeDetailsUseCase
import ru.zagrebin.front_mobile.domain.usecase.ObserveRecipesFeedUseCase
import ru.zagrebin.front_mobile.domain.usecase.RefreshArticleDetailsUseCase
import ru.zagrebin.front_mobile.domain.usecase.RefreshArticlesFeedUseCase
import ru.zagrebin.front_mobile.domain.usecase.RefreshRecipesFeedUseCase
import ru.zagrebin.front_mobile.domain.usecase.RefreshRecipeDetailsUseCase

class AppContainer(context: Context) {
    private val db = Room.databaseBuilder(context, AppDatabase::class.java, "vkr.db")
        .fallbackToDestructiveMigration()
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val feedApi: FeedApi = retrofit.create(FeedApi::class.java)
    private val repository = FeedRepository(
        db.feedDao(),
        feedApi,
        db.recipeDetailsDao(),
        db.articleDetailsDao()
    )

    val observeRecipesFeedUseCase = ObserveRecipesFeedUseCase(repository)
    val refreshRecipesFeedUseCase = RefreshRecipesFeedUseCase(repository)
    val observeArticlesFeedUseCase = ObserveArticlesFeedUseCase(repository)
    val refreshArticlesFeedUseCase = RefreshArticlesFeedUseCase(repository)
    val observeRecipeDetailsUseCase = ObserveRecipeDetailsUseCase(repository)
    val refreshRecipeDetailsUseCase = RefreshRecipeDetailsUseCase(repository)
    val observeArticleDetailsUseCase = ObserveArticleDetailsUseCase(repository)
    val refreshArticleDetailsUseCase = RefreshArticleDetailsUseCase(repository)

    private companion object {
        const val BASE_URL = "http://10.0.2.2:8080/"
    }
}