package ru.zagrebin.front_mobile.ui.screens.statistics

enum class MealType(val title: String) {
    BREAKFAST("Завтрак"),
    LUNCH("Обед"),
    DINNER("Ужин"),
    SNACK("Перекус")
}

data class MealEntry(
    val id: Long,
    val name: String,
    val amountLabel: String,
    val timeLabel: String,
    val kcal: Int,
    val proteins: Float,
    val fats: Float,
    val carbs: Float
)

data class MealDraft(
    val title: String = "",
    val portionGrams: Int = 0,
    val isLiquid: Boolean = false,
    val proteinsPer100: Float = 0f,
    val fatsPer100: Float = 0f,
    val carbsPer100: Float = 0f,
    val kcalPer100: Int = 0
)

data class StatisticsSettings(
    val retentionMonths: Int = 3,
    val goalKcal: Int = 2000,
    val waterGoalMl: Int = 1500,
    val proteinGoalGrams: Int = 90,
    val fatGoalGrams: Int = 70,
    val carbsGoalGrams: Int = 250
)

data class StatisticsDay(
    val id: Int,
    val dateIso: String,
    val dayNumber: String,
    val goalKcal: Int,
    val waterGoalMl: Int,
    val waterConsumedMl: Int,
    val proteinGoalGrams: Int,
    val fatGoalGrams: Int,
    val carbsGoalGrams: Int,
    val meals: Map<MealType, List<MealEntry>>
)
