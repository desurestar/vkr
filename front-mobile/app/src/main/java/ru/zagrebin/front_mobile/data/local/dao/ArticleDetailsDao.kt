package ru.zagrebin.front_mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.zagrebin.front_mobile.data.local.entities.ArticleDetailsEntity

@Dao
interface ArticleDetailsDao {
    @Query("SELECT * FROM article_details WHERE id = :id")
    fun observeById(id: Int): Flow<ArticleDetailsEntity?>

    @Query("SELECT * FROM article_details WHERE id = :id")
    suspend fun getById(id: Int): ArticleDetailsEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ArticleDetailsEntity)
}

