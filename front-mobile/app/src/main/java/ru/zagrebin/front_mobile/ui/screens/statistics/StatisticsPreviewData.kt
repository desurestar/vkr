package ru.zagrebin.front_mobile.ui.screens.statistics

fun previewMealEntries(): List<MealEntry> {
    return listOf(
        MealEntry(
            id = 1,
            name = "Instant Noodles Chic...",
            amountLabel = "60гр",
            timeLabel = "14:22",
            kcal = 270,
            proteins = 5.2f,
            fats = 11.8f,
            carbs = 34.8f
        ),
        MealEntry(
            id = 2,
            name = "Святой Источник",
            amountLabel = "500мл",
            timeLabel = "14:18",
            kcal = 100,
            proteins = 0f,
            fats = 0f,
            carbs = 22.5f
        )
    )
}

fun previewStatisticsDay(): StatisticsDay {
    val breakfast = previewMealEntries()
    return StatisticsDay(
        id = 30,
        dateIso = "2026-06-30",
        dayNumber = "30",
        goalKcal = 1000,
        waterGoalMl = 1500,
        waterConsumedMl = 500,
        proteinGoalGrams = 90,
        fatGoalGrams = 70,
        carbsGoalGrams = 250,
        meals = mapOf(
            MealType.BREAKFAST to breakfast,
            MealType.LUNCH to emptyList(),
            MealType.DINNER to emptyList(),
            MealType.SNACK to emptyList()
        )
    )
}

fun previewStatisticsUiState(): StatisticsUiState {
    val day28 = previewStatisticsDay().copy(id = 28, dayNumber = "28")
    val day29 = previewStatisticsDay().copy(id = 29, dayNumber = "29")
    val day30 = previewStatisticsDay()
    val day31 = previewStatisticsDay().copy(id = 31, dayNumber = "31")

    return StatisticsUiState(
        isLoading = false,
        days = listOf(day28, day29, day30, day31),
        selectedDayId = day30.id,
        settings = StatisticsSettings()
    )
}

