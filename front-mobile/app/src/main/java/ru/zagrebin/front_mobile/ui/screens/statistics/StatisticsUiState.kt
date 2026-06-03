package ru.zagrebin.front_mobile.ui.screens.statistics

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val days: List<StatisticsDay> = emptyList(),
    val selectedDayId: Int = 0,
    val settings: StatisticsSettings = StatisticsSettings(),
    val monthLabel: String = "",
    val recipeOptions: List<RecipeMealOption> = emptyList(),
    val currentUserId: String? = null,
    val recentRecipeIds: List<Int> = emptyList(),
    val errorMessage: String? = null
) {
    val selectedDay: StatisticsDay?
        get() = days.firstOrNull { it.id == selectedDayId }
}
