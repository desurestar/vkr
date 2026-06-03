package ru.zagrebin.front_mobile.ui.screens.statistics

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val days: List<StatisticsDay> = emptyList(),
    val selectedDayId: Int = 0,
    val settings: StatisticsSettings = StatisticsSettings(),
    val monthLabel: String = "",
    val recipeOptions: List<RecipeMealOption> = emptyList(),
    val recipeSearchResults: List<RecipeMealOption> = emptyList(),
    val isRecipeSearchLoading: Boolean = false,
    val hasMoreRecipeSearchResults: Boolean = false,
    val currentUserId: String? = null,
    val recentRecipeIds: List<Int> = emptyList(),
    val errorMessage: String? = null
) {
    val selectedDay: StatisticsDay?
        get() = days.firstOrNull { it.id == selectedDayId }
}
