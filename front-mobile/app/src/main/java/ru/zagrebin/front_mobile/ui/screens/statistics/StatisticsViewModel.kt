package ru.zagrebin.front_mobile.ui.screens.statistics

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class StatisticsViewModel : ViewModel() {

    val state: StateFlow<StatisticsUiState> = StatisticsStore.state

    fun selectDay(dayId: Int) {
        StatisticsStore.selectDay(dayId)
    }

    fun addWater(amountMl: Int = 250) {
        StatisticsStore.addWater(amountMl)
    }

    fun addMeal(type: MealType, draft: MealDraft) {
        StatisticsStore.addMeal(type, draft)
    }

    fun addMockMeal(type: MealType) {
        StatisticsStore.addMockMeal(type)
    }
}
