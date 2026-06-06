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
import ru.zagrebin.front_mobile.data.remote.api.PersistentCookieJar
import ru.zagrebin.front_mobile.data.repository.FeedRepository
import ru.zagrebin.front_mobile.data.repository.StatisticsRepository
import ru.zagrebin.front_mobile.data.sync.NetworkConnectionChecker
import ru.zagrebin.front_mobile.data.sync.OfflineSyncManager
import ru.zagrebin.front_mobile.domain.usecase.ObserveArticleDetailsUseCase
import ru.zagrebin.front_mobile.domain.usecase.ObserveArticlesFeedUseCase
import ru.zagrebin.front_mobile.domain.usecase.ObserveRecipeDetailsUseCase
import ru.zagrebin.front_mobile.domain.usecase.ObserveRecipesFeedUseCase
import ru.zagrebin.front_mobile.domain.usecase.RefreshArticleDetailsUseCase
import ru.zagrebin.front_mobile.domain.usecase.RefreshArticlesFeedUseCase
import ru.zagrebin.front_mobile.domain.usecase.RefreshRecipesFeedUseCase
import ru.zagrebin.front_mobile.domain.usecase.RefreshRecipeDetailsUseCase

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val db = dbInstance ?: synchronized(lock) {
        dbInstance ?: Room.databaseBuilder(appContext, AppDatabase::class.java, "vkr.db")
            .fallbackToDestructiveMigration()
            .build()
            .also { dbInstance = it }
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .cookieJar(PersistentCookieJar(appContext))
            .addInterceptor(logging)
            .build()
    }

    val feedApi: FeedApi = feedApiInstance ?: synchronized(lock) {
        feedApiInstance ?: Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshiInstance))
            .build()
            .create(FeedApi::class.java)
            .also { feedApiInstance = it }
    }

    val networkConnectionChecker = NetworkConnectionChecker(appContext)

    val feedRepository = FeedRepository(
        db.feedDao(),
        db.syncDao(),
        feedApi,
        db.recipeDetailsDao(),
        db.articleDetailsDao(),
        db.tagDao(),
        networkConnectionChecker
    )

    val statisticsRepository = StatisticsRepository(db.statisticsDao(), db.syncDao(), feedApi, networkConnectionChecker)

    val offlineSyncManager = OfflineSyncManager(networkConnectionChecker, statisticsRepository, feedRepository)

    val observeRecipesFeedUseCase = ObserveRecipesFeedUseCase(feedRepository)
    val refreshRecipesFeedUseCase = RefreshRecipesFeedUseCase(feedRepository)
    val observeArticlesFeedUseCase = ObserveArticlesFeedUseCase(feedRepository)
    val refreshArticlesFeedUseCase = RefreshArticlesFeedUseCase(feedRepository)
    val observeRecipeDetailsUseCase = ObserveRecipeDetailsUseCase(feedRepository)
    val refreshRecipeDetailsUseCase = RefreshRecipeDetailsUseCase(feedRepository)
    val observeArticleDetailsUseCase = ObserveArticleDetailsUseCase(feedRepository)
    val refreshArticleDetailsUseCase = RefreshArticleDetailsUseCase(feedRepository)

    suspend fun clearAuthorizedUserData() {
        db.profileDao().clear()
        statisticsRepository.clearLocalData()
        PersistentCookieJar.clear(appContext)
    }

    companion object {
        // const val BASE_URL = "http://192.168.4.103:8080/"
        // const val BASE_URL = "http://192.168.0.9:8080/"
        const val BASE_URL = "http://178.253.38.110:8080/"
        // const val BASE_URL = "http://10.0.2.2:8080/"

        fun resolveMediaUrl(url: String?): String? {
            val value = url?.trim()?.takeIf { it.isNotBlank() } ?: return null
            if (value.startsWith("http://", ignoreCase = true) ||
                value.startsWith("https://", ignoreCase = true) ||
                value.startsWith("content://", ignoreCase = true) ||
                value.startsWith("file://", ignoreCase = true)
            ) {
                return value
            }

            val base = BASE_URL.trimEnd('/')
            val path = if (value.startsWith('/')) value else "/$value"
            return base + path
        }

        fun toRelativeMediaPath(url: String?): String? {
            val value = url?.trim()?.takeIf { it.isNotBlank() } ?: return null
            if (value.startsWith("/media/")) return value
            if (value.startsWith("media/")) return "/$value"

            if (value.startsWith("http://", ignoreCase = true) || value.startsWith("https://", ignoreCase = true)) {
                return runCatching {
                    java.net.URI.create(value).path?.takeIf { it.startsWith("/media/") }
                }.getOrNull() ?: value
            }

            return value
        }

        private val lock = Any()
        @Volatile private var dbInstance: AppDatabase? = null
        @Volatile private var feedApiInstance: FeedApi? = null

        private val moshiInstance: Moshi by lazy {
            Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
        }
    }
}
