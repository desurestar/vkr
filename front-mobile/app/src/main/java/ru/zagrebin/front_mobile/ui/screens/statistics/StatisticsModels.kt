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

data class StatisticsDay(
    val id: Int,
    val dayNumber: String,
    val goalKcal: Int,
    val waterGoalMl: Int,
    val waterConsumedMl: Int,
    val meals: Map<MealType, List<MealEntry>>
)

