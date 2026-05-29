package ru.zagrebin.front_mobile.data.local.dao;

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.zagrebin.front_mobile.data.local.entities.FeedItemEntity

@Dao
interface FeedDao {
    @Query("SELECT * FROM feed_items WHERE type = :type ORDER BY id DESC")
    fun observeByType(type: String): Flow<List<FeedItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<FeedItemEntity>)

    @Query("DELETE FROM feed_items WHERE type = :type")
    suspend fun deleteByType(type: String)

    @Transaction
    suspend fun replaceByType(type: String, items: List<FeedItemEntity>) {
        deleteByType(type)
        upsertAll(items)
    }
}
