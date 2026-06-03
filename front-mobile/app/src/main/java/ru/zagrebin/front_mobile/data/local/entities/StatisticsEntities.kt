package ru.zagrebin.front_mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statistics_settings")
data class StatisticsSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val retentionMonths: Int = 3,
    val goalKcal: Int = 2000,
    val waterGoalMl: Int = 1500,
    val proteinGoalGrams: Int = 90,
    val fatGoalGrams: Int = 70,
    val carbsGoalGrams: Int = 250
)

@Entity(tableName = "statistics_days", primaryKeys = ["dateIso"])
data class StatisticsDayEntity(
    val dateIso: String,
    val waterConsumedMl: Int = 0
)

@Entity(tableName = "statistics_meals")
data class StatisticsMealEntryEntity(
    @PrimaryKey val id: Long,
    val dateIso: String,
    val type: String,
    val name: String,
    val amountLabel: String,
    val timeLabel: String,
    val kcal: Int,
    val proteins: Float,
    val fats: Float,
    val carbs: Float
)
