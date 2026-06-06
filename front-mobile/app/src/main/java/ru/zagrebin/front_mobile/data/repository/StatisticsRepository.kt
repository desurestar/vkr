package ru.zagrebin.front_mobile.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.zagrebin.front_mobile.data.local.dao.StatisticsDao
import ru.zagrebin.front_mobile.data.local.dao.SyncDao
import ru.zagrebin.front_mobile.data.local.entities.StatisticsDayEntity
import ru.zagrebin.front_mobile.data.local.entities.StatisticsMealEntryEntity
import ru.zagrebin.front_mobile.data.local.entities.StatisticsSettingsEntity
import ru.zagrebin.front_mobile.data.local.entities.PendingStatisticsOpEntity
import ru.zagrebin.front_mobile.data.remote.api.AddMealRequest
import ru.zagrebin.front_mobile.data.remote.api.AddRecipeMealRequest
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
import java.util.Locale
import kotlin.math.roundToInt

class StatisticsRepository(
    private val dao: StatisticsDao,
    private val syncDao: SyncDao,
    private val api: FeedApi,
    private val networkConnectionChecker: NetworkConnectionChecker
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val waterAdapter = moshi.adapter(PendingWaterOp::class.java)
    private val mealAdapter = moshi.adapter(PendingMealOp::class.java)
    private val recipeMealAdapter = moshi.adapter(PendingRecipeMealOp::class.java)
    private val settingsAdapter = moshi.adapter(PendingSettingsOp::class.java)

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
            StatisticsUiState(
                days = days,
                selectedDayId = selectedDate.toEpochDay().toInt(),
                settings = settings,
                monthLabel = month.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru")))
            )
        }
    }

    suspend fun clearLocalData() {
        dao.clearAll()
        syncDao.clearPendingStatisticsOps()
    }

    suspend fun refreshMonth(month: YearMonth): Boolean {
        val start = month.atDay(1).toString()
        val end = month.atEndOfMonth().toString()
        if (!networkConnectionChecker.isNetworkAvailable()) return false
        syncPendingChanges()
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
        val request = AddWaterRequest(dateIso, amountMl)
        if (!networkConnectionChecker.isNetworkAvailable() || runCatching { api.addStatisticsWater(request) }.isFailure) {
            enqueueStatisticsOp(STAT_OP_WATER, waterAdapter.toJson(PendingWaterOp(dateIso, amountMl)))
        }
    }

    suspend fun addMeal(date: LocalDate, type: MealType, draft: MealDraft) {
        val dateIso = date.toString()
        val entry = buildMealEntry(dateIso, type, draft)
        dao.upsertMeal(entry)
        val request = entry.toRequest(dateIso)
        val synced = networkConnectionChecker.isNetworkAvailable() && runCatching {
            val saved = api.addStatisticsMeal(request)
            dao.deleteMeal(entry.id)
            dao.upsertMeal(saved.toEntity(dateIso, type))
        }.isSuccess
        if (!synced) {
            enqueueStatisticsOp(STAT_OP_MEAL, mealAdapter.toJson(PendingMealOp(request)), entry.id)
        }
    }

    suspend fun addRecipeMeal(date: LocalDate, type: MealType, recipeId: Int, draft: MealDraft) {
        val dateIso = date.toString()
        val entry = buildMealEntry(dateIso, type, draft.copy(recipeId = recipeId))
        dao.upsertMeal(entry)
        val request = AddRecipeMealRequest(
            date = dateIso,
            type = type.name,
            portionGrams = draft.portionGrams.coerceAtLeast(0),
            liquid = draft.isLiquid,
            timeLabel = entry.timeLabel
        )
        val synced = networkConnectionChecker.isNetworkAvailable() && runCatching {
            val saved = api.addRecipeStatisticsMeal(recipeId = recipeId, request = request)
            dao.deleteMeal(entry.id)
            dao.upsertMeal(saved.toEntity(dateIso, type))
        }.isSuccess
        if (!synced) {
            enqueueStatisticsOp(STAT_OP_RECIPE_MEAL, recipeMealAdapter.toJson(PendingRecipeMealOp(recipeId, request)), entry.id)
        }
    }

    suspend fun updateSettings(settings: StatisticsSettings) {
        dao.upsertSettings(settings.toEntity())
        prune(settings.retentionMonths)
        val request = settings.toRequest()
        if (!networkConnectionChecker.isNetworkAvailable() || runCatching { api.updateStatisticsSettings(request) }.isFailure) {
            enqueueStatisticsOp(STAT_OP_SETTINGS, settingsAdapter.toJson(PendingSettingsOp(request)))
        }
    }


    suspend fun syncPendingChanges(): Boolean {
        if (!networkConnectionChecker.isNetworkAvailable()) return false
        val ops = syncDao.getPendingStatisticsOps()
        for (op in ops) {
            val success = runCatching {
                when (op.opType) {
                    STAT_OP_WATER -> {
                        val payload = waterAdapter.fromJson(op.payloadJson) ?: error("Invalid water sync payload")
                        api.addStatisticsWater(AddWaterRequest(payload.date, payload.amountMl))
                    }
                    STAT_OP_MEAL -> {
                        val payload = mealAdapter.fromJson(op.payloadJson) ?: error("Invalid meal sync payload")
                        val saved = api.addStatisticsMeal(payload.request)
                        op.localMealId?.let { dao.deleteMeal(it) }
                        dao.upsertMeal(saved.toEntity(payload.request.date, MealType.valueOf(payload.request.type)))
                    }
                    STAT_OP_RECIPE_MEAL -> {
                        val payload = recipeMealAdapter.fromJson(op.payloadJson) ?: error("Invalid recipe meal sync payload")
                        val saved = api.addRecipeStatisticsMeal(payload.recipeId, payload.request)
                        op.localMealId?.let { dao.deleteMeal(it) }
                        dao.upsertMeal(saved.toEntity(payload.request.date, MealType.valueOf(payload.request.type)))
                    }
                    STAT_OP_SETTINGS -> {
                        val payload = settingsAdapter.fromJson(op.payloadJson) ?: error("Invalid settings sync payload")
                        api.updateStatisticsSettings(payload.request)
                    }
                }
            }.isSuccess

            if (success) {
                syncDao.deleteStatisticsOp(op.id)
            } else {
                return false
            }
        }
        return true
    }

    private suspend fun enqueueStatisticsOp(opType: String, payloadJson: String, localMealId: Long? = null) {
        syncDao.enqueueStatisticsOp(PendingStatisticsOpEntity(opType = opType, payloadJson = payloadJson, localMealId = localMealId))
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

    private companion object {
        const val STAT_OP_WATER = "WATER"
        const val STAT_OP_MEAL = "MEAL"
        const val STAT_OP_RECIPE_MEAL = "RECIPE_MEAL"
        const val STAT_OP_SETTINGS = "SETTINGS"
    }
}

private data class PendingWaterOp(val date: String, val amountMl: Int)
private data class PendingMealOp(val request: AddMealRequest)
private data class PendingRecipeMealOp(val recipeId: Int, val request: AddRecipeMealRequest)
private data class PendingSettingsOp(val request: StatisticsSettingsRequest)

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
