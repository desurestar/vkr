package ru.zagrebin.front_mobile.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.zagrebin.front_mobile.ui.theme.AppPageBackgroundColor

@Composable
fun StatisticsContent(
    state: StatisticsUiState,
    onDayClick: (Int) -> Unit,
    onAddWater: (Int) -> Unit,
    onAddMeal: (MealType) -> Unit
) {
    var openedMealType by remember { mutableStateOf<MealType?>(null) }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val selectedDay = state.selectedDay ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppPageBackgroundColor),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            DayStrip(
                days = state.days,
                selectedDayId = state.selectedDayId,
                onDayClick = onDayClick
            )
        }

        item {
            NutritionSummaryCard(day = selectedDay)
        }

        item {
            WaterCard(
                consumedMl = selectedDay.waterConsumedMl,
                goalMl = selectedDay.waterGoalMl,
                onAdd = onAddWater
            )
        }

        items(MealType.entries) { mealType ->
            val entries = selectedDay.meals[mealType].orEmpty()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MealSectionCard(
                    type = mealType,
                    entries = entries,
                    onAddClick = { openedMealType = mealType }
                )

                entries.forEach { meal ->
                    MealEntryTile(meal = meal)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(92.dp))
        }
    }

    if (openedMealType != null) {
        AddMealBottomSheet(
            mealType = openedMealType!!,
            onDismiss = { openedMealType = null },
            onAddClick = {
                onAddMeal(openedMealType!!)
                openedMealType = null
            }
        )
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun StatisticsContentPreview() {
    StatisticsContent(
        state = previewStatisticsUiState(),
        onDayClick = {},
        onAddWater = {},
        onAddMeal = {}
    )
}

