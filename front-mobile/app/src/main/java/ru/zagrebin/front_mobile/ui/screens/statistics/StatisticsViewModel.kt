package ru.zagrebin.front_mobile.ui.screens.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.data.AppContainer
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppContainer(application).statisticsRepository
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val selectedMonth = MutableStateFlow(YearMonth.now())

    val state: StateFlow<StatisticsUiState> = selectedMonth.flatMapLatest { month ->
        selectedDate.flatMapLatest { date ->
            repository.observeMonth(month, date).map { it.copy(isLoading = false) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState(isLoading = true))

    init {
        refresh()
    }

    fun selectDay(dayId: Int) {
        val date = LocalDate.ofEpochDay(dayId.toLong())
        selectedDate.value = date
        val month = YearMonth.from(date)
        if (selectedMonth.value != month) selectedMonth.value = month
    }

    fun addWater(amountMl: Int = 250) {
        viewModelScope.launch { repository.addWater(selectedDate.value, amountMl) }
    }

    fun addMeal(type: MealType, draft: MealDraft) {
        viewModelScope.launch { repository.addMeal(selectedDate.value, type, draft) }
    }

    fun updateSettings(settings: StatisticsSettings) {
        viewModelScope.launch { repository.updateSettings(settings) }
    }

    fun refresh() {
        viewModelScope.launch { repository.refreshMonth(selectedMonth.value) }
    }

    fun addMockMeal(type: MealType) = Unit
}
