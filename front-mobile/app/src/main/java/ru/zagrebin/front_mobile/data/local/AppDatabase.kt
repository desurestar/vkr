package ru.zagrebin.front_mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.zagrebin.front_mobile.data.local.dao.ArticleDetailsDao
import ru.zagrebin.front_mobile.data.local.dao.FeedDao
import ru.zagrebin.front_mobile.data.local.dao.ProfileDao
import ru.zagrebin.front_mobile.data.local.dao.RecipeDetailsDao
import ru.zagrebin.front_mobile.data.local.dao.TagDao
import ru.zagrebin.front_mobile.data.local.entities.ArticleDetailsEntity
import ru.zagrebin.front_mobile.data.local.entities.FeedItemEntity
import ru.zagrebin.front_mobile.data.local.entities.ProfileEntity
import ru.zagrebin.front_mobile.data.local.entities.RecipeDetailsEntity
import ru.zagrebin.front_mobile.data.local.entities.TagEntity

@Database(
    entities = [
        FeedItemEntity::class,
        RecipeDetailsEntity::class,
        ArticleDetailsEntity::class,
        ProfileEntity::class,
        TagEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
    abstract fun recipeDetailsDao(): RecipeDetailsDao
    abstract fun articleDetailsDao(): ArticleDetailsDao
    abstract fun profileDao(): ProfileDao
    abstract fun tagDao(): TagDao
}
