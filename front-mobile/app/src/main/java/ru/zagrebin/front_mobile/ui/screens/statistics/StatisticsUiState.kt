package ru.zagrebin.front_mobile.ui.screens.statistics

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val days: List<StatisticsDay> = emptyList(),
    val selectedDayId: Int = 0
) {
    val selectedDay: StatisticsDay?
        get() = days.firstOrNull { it.id == selectedDayId }
}

