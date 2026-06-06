package ru.zagrebin.front_mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.zagrebin.front_mobile.data.local.entities.StatisticsDayEntity
import ru.zagrebin.front_mobile.data.local.entities.StatisticsMealEntryEntity
import ru.zagrebin.front_mobile.data.local.entities.StatisticsSettingsEntity

@Dao
interface StatisticsDao {
    @Query("SELECT * FROM statistics_settings WHERE id = 1")
    fun observeSettings(): Flow<StatisticsSettingsEntity?>

    @Query("SELECT * FROM statistics_days WHERE dateIso BETWEEN :start AND :end")
    fun observeDays(start: String, end: String): Flow<List<StatisticsDayEntity>>

    @Query("SELECT * FROM statistics_meals WHERE dateIso BETWEEN :start AND :end ORDER BY id ASC")
    fun observeMeals(start: String, end: String): Flow<List<StatisticsMealEntryEntity>>

    @Query("SELECT * FROM statistics_settings WHERE id = 1")
    suspend fun getSettings(): StatisticsSettingsEntity?

    @Query("SELECT * FROM statistics_days WHERE dateIso = :dateIso LIMIT 1")
    suspend fun getDay(dateIso: String): StatisticsDayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: StatisticsSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDays(days: List<StatisticsDayEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeals(meals: List<StatisticsMealEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDay(day: StatisticsDayEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeal(meal: StatisticsMealEntryEntity)

    @Query("DELETE FROM statistics_meals WHERE id = :id")
    suspend fun deleteMeal(id: Long)

    @Query("DELETE FROM statistics_days WHERE dateIso BETWEEN :start AND :end")
    suspend fun clearDays(start: String, end: String)

    @Query("DELETE FROM statistics_meals WHERE dateIso BETWEEN :start AND :end")
    suspend fun clearMeals(start: String, end: String)

    @Query("DELETE FROM statistics_days WHERE dateIso < :cutoff")
    suspend fun pruneDays(cutoff: String)

    @Query("DELETE FROM statistics_meals WHERE dateIso < :cutoff")
    suspend fun pruneMeals(cutoff: String)

    @Query("DELETE FROM statistics_settings")
    suspend fun clearSettings()

    @Query("DELETE FROM statistics_days")
    suspend fun clearAllDays()

    @Query("DELETE FROM statistics_meals")
    suspend fun clearAllMeals()

    @Transaction
    suspend fun clearAll() {
        clearSettings()
        clearAllDays()
        clearAllMeals()
    }

    @Transaction
    suspend fun replaceMonth(start: String, end: String, settings: StatisticsSettingsEntity, days: List<StatisticsDayEntity>, meals: List<StatisticsMealEntryEntity>) {
        upsertSettings(settings)
        clearDays(start, end)
        clearMeals(start, end)
        upsertDays(days)
        upsertMeals(meals)
    }
}
