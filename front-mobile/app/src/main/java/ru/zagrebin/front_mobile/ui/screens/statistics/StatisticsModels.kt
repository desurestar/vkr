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

data class StatisticsDay(
    val id: Int,
    val dayNumber: String,
    val goalKcal: Int,
    val waterGoalMl: Int,
    val waterConsumedMl: Int,
    val meals: Map<MealType, List<MealEntry>>
)
