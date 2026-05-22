package ru.zagrebin.front_mobile.ui.screens.statistics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object StatisticsStore {

    private val _state = MutableStateFlow(
        StatisticsUiState(
            isLoading = false,
            days = mockDays(),
            selectedDayId = 3
        )
    )
    val state: StateFlow<StatisticsUiState> = _state.asStateFlow()

    fun selectDay(dayId: Int) {
        _state.update { current ->
            if (current.days.none { it.id == dayId }) current else current.copy(selectedDayId = dayId)
        }
    }

    fun addWater(amountMl: Int = 250) {
        updateSelectedDay { day ->
            day.copy(waterConsumedMl = day.waterConsumedMl + amountMl)
        }
    }

    fun addMeal(type: MealType, draft: MealDraft) {
        updateSelectedDay { day ->
            val currentEntries = day.meals[type].orEmpty()
            val newEntry = buildMealEntry(draft)
            val updatedMeals = day.meals.toMutableMap().apply {
                this[type] = currentEntries + newEntry
            }
            day.copy(meals = updatedMeals)
        }
    }

    fun addMockMeal(type: MealType) {
        updateSelectedDay { day ->
            val currentEntries = day.meals[type].orEmpty()
            val newEntry = nextMeal(type, currentEntries.size)
            val updatedMeals = day.meals.toMutableMap().apply {
                this[type] = currentEntries + newEntry
            }
            day.copy(meals = updatedMeals)
        }
    }

    private fun updateSelectedDay(transform: (StatisticsDay) -> StatisticsDay) {
        _state.update { current ->
            val selectedId = current.selectedDayId
            current.copy(
                days = current.days.map { day ->
                    if (day.id == selectedId) transform(day) else day
                }
            )
        }
    }

    private fun buildMealEntry(draft: MealDraft): MealEntry {
        val portion = draft.portionGrams.coerceAtLeast(0)
        val factor = if (portion > 0) portion / 100f else 0f
        val kcal = (draft.kcalPer100 * factor).roundToInt()
        val proteins = draft.proteinsPer100 * factor
        val fats = draft.fatsPer100 * factor
        val carbs = draft.carbsPer100 * factor
        val amountUnit = if (draft.isLiquid) "мл" else "гр"
        val title = draft.title.ifBlank { "Прием пищи" }

        return MealEntry(
            id = System.currentTimeMillis(),
            name = title,
            amountLabel = "$portion$amountUnit",
            timeLabel = nowTimeLabel(),
            kcal = kcal,
            proteins = proteins,
            fats = fats,
            carbs = carbs
        )
    }

    private fun nowTimeLabel(): String {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(Date())
    }

    private fun nextMeal(type: MealType, index: Int): MealEntry {
        val base = when (type) {
            MealType.BREAKFAST -> listOf(
                MealEntry(0, "Овсянка с бананом", "220гр", "08:30", 280, 8.0f, 5.5f, 48.0f),
                MealEntry(0, "Омлет с овощами", "190гр", "09:10", 320, 19.5f, 18.0f, 8.0f)
            )

            MealType.LUNCH -> listOf(
                MealEntry(0, "Куриная грудка и рис", "320гр", "13:25", 510, 40.0f, 12.0f, 58.0f),
                MealEntry(0, "Суп с говядиной", "360гр", "14:05", 420, 25.0f, 14.0f, 38.0f)
            )

            MealType.DINNER -> listOf(
                MealEntry(0, "Запеченная рыба", "260гр", "19:10", 390, 32.0f, 16.0f, 22.0f),
                MealEntry(0, "Гречка с индейкой", "300гр", "20:00", 440, 30.0f, 14.0f, 44.0f)
            )

            MealType.SNACK -> listOf(
                MealEntry(0, "Йогурт и орехи", "150гр", "16:15", 210, 9.0f, 12.0f, 16.0f),
                MealEntry(0, "Творог с ягодами", "180гр", "17:00", 240, 22.0f, 7.0f, 18.0f)
            )
        }

        val item = base[index % base.size]
        return item.copy(id = System.currentTimeMillis() + index)
    }

    private fun mockDays(): List<StatisticsDay> {
        val dayNumbers = listOf("28", "29", "30", "31", "1", "2", "3", "4")

        return dayNumbers.mapIndexed { idx, dayNumber ->
            val breakfast = if (dayNumber == "30") {
                listOf(
                    MealEntry(1, "Instant Noodles Chicken", "60гр", "14:22", 270, 5.2f, 11.8f, 34.8f),
                    MealEntry(2, "Святой источник", "500мл", "14:18", 100, 0f, 0f, 22.5f)
                )
            } else emptyList()

            StatisticsDay(
                id = idx + 1,
                dayNumber = dayNumber,
                goalKcal = 1000,
                waterGoalMl = 1500,
                waterConsumedMl = if (dayNumber == "30") 500 else 250,
                meals = mapOf(
                    MealType.BREAKFAST to breakfast,
                    MealType.LUNCH to emptyList(),
                    MealType.DINNER to emptyList(),
                    MealType.SNACK to emptyList()
                )
            )
        }
    }
}

