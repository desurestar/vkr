package ru.zagrebin.front_mobile.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.zagrebin.front_mobile.data.local.dao.StatisticsDao
import ru.zagrebin.front_mobile.data.local.entities.StatisticsDayEntity
import ru.zagrebin.front_mobile.data.local.entities.StatisticsMealEntryEntity
import ru.zagrebin.front_mobile.data.local.entities.StatisticsSettingsEntity
import ru.zagrebin.front_mobile.data.remote.api.AddMealRequest
import ru.zagrebin.front_mobile.data.remote.api.AddWaterRequest
import ru.zagrebin.front_mobile.data.remote.api.FeedApi
import ru.zagrebin.front_mobile.data.remote.api.StatisticsDayDto
import ru.zagrebin.front_mobile.data.remote.api.StatisticsMealEntryDto
import ru.zagrebin.front_mobile.data.remote.api.StatisticsResponseDto
import ru.zagrebin.front_mobile.data.remote.api.StatisticsSettingsDto
import ru.zagrebin.front_mobile.data.remote.api.StatisticsSettingsRequest
import ru.zagrebin.front_mobile.data.sync.NetworkConnectionChecker
import ru.zagrebin.front_mobile.ui.screens.statistics.MealDraft
import ru.zagrebin.front_mobile.ui.screens.statistics.MealEntry
import ru.zagrebin.front_mobile.ui.screens.statistics.MealType
import ru.zagrebin.front_mobile.ui.screens.statistics.StatisticsDay
import ru.zagrebin.front_mobile.ui.screens.statistics.StatisticsSettings
import ru.zagrebin.front_mobile.ui.screens.statistics.StatisticsUiState
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class StatisticsRepository(
    private val dao: StatisticsDao,
    private val api: FeedApi,
    private val networkConnectionChecker: NetworkConnectionChecker
) {
    fun observeMonth(month: YearMonth, selectedDate: LocalDate): Flow<StatisticsUiState> {
        val start = month.atDay(1).toString()
        val end = month.atEndOfMonth().toString()
        return combine(
            dao.observeSettings(),
            dao.observeDays(start, end),
            dao.observeMeals(start, end)
        ) { settingsEntity, dayEntities, mealEntities ->
            val settings = (settingsEntity ?: StatisticsSettingsEntity()).toUi()
            val waterByDate = dayEntities.associateBy { it.dateIso }
            val mealsByDate = mealEntities.groupBy { it.dateIso }
            val days = (1..month.lengthOfMonth()).map { dayOfMonth ->
                val date = month.atDay(dayOfMonth)
                val dateIso = date.toString()
                val meals = mealsByDate[dateIso].orEmpty().groupBy { MealType.valueOf(it.type) }
                StatisticsDay(
                    id = date.toEpochDay().toInt(),
                    dateIso = dateIso,
                    dayNumber = dayOfMonth.toString(),
                    goalKcal = settings.goalKcal,
                    waterGoalMl = settings.waterGoalMl,
                    waterConsumedMl = waterByDate[dateIso]?.waterConsumedMl ?: 0,
                    proteinGoalGrams = settings.proteinGoalGrams,
                    fatGoalGrams = settings.fatGoalGrams,
                    carbsGoalGrams = settings.carbsGoalGrams,
                    meals = MealType.entries.associateWith { type -> meals[type].orEmpty().map { it.toUi() } }
                )
            }
            StatisticsUiState(days = days, selectedDayId = selectedDate.toEpochDay().toInt(), settings = settings)
        }
    }

    suspend fun refreshMonth(month: YearMonth): Boolean {
        val start = month.atDay(1).toString()
        val end = month.atEndOfMonth().toString()
        if (!networkConnectionChecker.isNetworkAvailable()) return false
        return runCatching {
            val response = api.getStatistics(month.format(DateTimeFormatter.ofPattern("yyyy-MM")))
            dao.replaceMonth(start, end, response.settings.toEntity(), response.days.map { it.toDayEntity() }, response.days.flatMap { it.toMealEntities() })
            prune(response.settings.retentionMonths)
            true
        }.getOrDefault(false)
    }

    suspend fun addWater(date: LocalDate, amountMl: Int) {
        val dateIso = date.toString()
        val current = dao.getDay(dateIso)
        dao.upsertDay(StatisticsDayEntity(dateIso, (current?.waterConsumedMl ?: 0) + amountMl))
        if (networkConnectionChecker.isNetworkAvailable()) {
            runCatching { api.addStatisticsWater(AddWaterRequest(dateIso, amountMl)) }
        }
    }

    suspend fun addMeal(date: LocalDate, type: MealType, draft: MealDraft) {
        val entry = buildMealEntry(date.toString(), type, draft)
        dao.upsertMeal(entry)
        if (networkConnectionChecker.isNetworkAvailable()) {
            runCatching {
                val saved = api.addStatisticsMeal(entry.toRequest(date.toString()))
                dao.upsertMeal(saved.toEntity(date.toString(), type))
            }
        }
    }

    suspend fun updateSettings(settings: StatisticsSettings) {
        dao.upsertSettings(settings.toEntity())
        prune(settings.retentionMonths)
        if (networkConnectionChecker.isNetworkAvailable()) {
            runCatching { api.updateStatisticsSettings(settings.toRequest()) }
        }
    }

    private suspend fun prune(retentionMonths: Int) {
        val cutoff = LocalDate.now().minusMonths(retentionMonths.toLong()).withDayOfMonth(1).toString()
        dao.pruneDays(cutoff)
        dao.pruneMeals(cutoff)
    }

    private fun buildMealEntry(dateIso: String, type: MealType, draft: MealDraft): StatisticsMealEntryEntity {
        val portion = draft.portionGrams.coerceAtLeast(0)
        val factor = if (portion > 0) portion / 100f else 0f
        val amountUnit = if (draft.isLiquid) "мл" else "гр"
        return StatisticsMealEntryEntity(
            id = -System.currentTimeMillis(),
            dateIso = dateIso,
            type = type.name,
            name = draft.title.ifBlank { "Прием пищи" },
            amountLabel = "$portion$amountUnit",
            timeLabel = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
            kcal = (draft.kcalPer100 * factor).roundToInt(),
            proteins = draft.proteinsPer100 * factor,
            fats = draft.fatsPer100 * factor,
            carbs = draft.carbsPer100 * factor
        )
    }
}

private fun StatisticsSettingsEntity.toUi() = StatisticsSettings(retentionMonths, goalKcal, waterGoalMl, proteinGoalGrams, fatGoalGrams, carbsGoalGrams)
private fun StatisticsSettings.toEntity() = StatisticsSettingsEntity(1, retentionMonths, goalKcal, waterGoalMl, proteinGoalGrams, fatGoalGrams, carbsGoalGrams)
private fun StatisticsSettingsDto.toEntity() = StatisticsSettingsEntity(1, retentionMonths, goalKcal, waterGoalMl, proteinGoalGrams, fatGoalGrams, carbsGoalGrams)
private fun StatisticsSettings.toRequest() = StatisticsSettingsRequest(retentionMonths, goalKcal, waterGoalMl, proteinGoalGrams, fatGoalGrams, carbsGoalGrams)
private fun StatisticsMealEntryEntity.toUi() = MealEntry(id, name, amountLabel, timeLabel, kcal, proteins, fats, carbs)
private fun StatisticsDayDto.toDayEntity() = StatisticsDayEntity(date, waterConsumedMl)
private fun StatisticsMealEntryDto.toEntity(dateIso: String, type: MealType) = StatisticsMealEntryEntity(id, dateIso, type.name, name, amountLabel, timeLabel, kcal, proteins, fats, carbs)
private fun StatisticsDayDto.toMealEntities(): List<StatisticsMealEntryEntity> =
    breakfast.map { it.toEntity(date, MealType.BREAKFAST) } + lunch.map { it.toEntity(date, MealType.LUNCH) } + dinner.map { it.toEntity(date, MealType.DINNER) } + snack.map { it.toEntity(date, MealType.SNACK) }
private fun StatisticsMealEntryEntity.toRequest(dateIso: String) = AddMealRequest(dateIso, type, name, amountLabel, timeLabel, kcal, proteins, fats, carbs)
