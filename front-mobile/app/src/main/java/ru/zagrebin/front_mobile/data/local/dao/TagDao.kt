package ru.zagrebin.front_mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ru.zagrebin.front_mobile.data.local.entities.TagEntity

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name")
    suspend fun getAll(): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tags: List<TagEntity>)

    @Query("DELETE FROM tags")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(tags: List<TagEntity>) {
        clear()
        upsertAll(tags)
    }
}
