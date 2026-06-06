package ru.zagrebin.front_mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.zagrebin.front_mobile.data.local.entities.LocalDraftEntity
import ru.zagrebin.front_mobile.data.local.entities.PendingStatisticsOpEntity

@Dao
interface SyncDao {
    @Query("SELECT * FROM pending_statistics_ops ORDER BY id ASC")
    suspend fun getPendingStatisticsOps(): List<PendingStatisticsOpEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueStatisticsOp(op: PendingStatisticsOpEntity): Long

    @Query("DELETE FROM pending_statistics_ops WHERE id = :id")
    suspend fun deleteStatisticsOp(id: Long)

    @Query("DELETE FROM pending_statistics_ops")
    suspend fun clearPendingStatisticsOps()

    @Query("SELECT * FROM local_drafts ORDER BY createdAt DESC")
    fun observeLocalDrafts(): Flow<List<LocalDraftEntity>>

    @Query("SELECT * FROM local_drafts ORDER BY createdAt ASC")
    suspend fun getLocalDraftsForSync(): List<LocalDraftEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalDraft(draft: LocalDraftEntity)

    @Query("DELETE FROM local_drafts WHERE id = :id")
    suspend fun deleteLocalDraft(id: Int)
}
