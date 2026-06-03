package ru.zagrebin.front_mobile.ui.screens.statistics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    StatisticsContent(
        state = state,
        onDayClick = viewModel::selectDay,
        onAddWater = viewModel::addWater,
        onAddMeal = viewModel::addMeal,
        onUpdateSettings = viewModel::updateSettings,
        onPreviousMonth = viewModel::showPreviousMonth,
        onNextMonth = viewModel::showNextMonth,
        onRecipeSearch = viewModel::searchRecipes,
        onLoadMoreRecipes = viewModel::loadMoreRecipes
    )
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun StatisticsScreenPreview() {
    StatisticsContent(
        state = previewStatisticsUiState(),
        onDayClick = {},
        onAddWater = {},
        onAddMeal = { _, _ -> },
        onUpdateSettings = {},
        onPreviousMonth = {},
        onNextMonth = {}
    )
}
