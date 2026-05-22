package ru.zagrebin.front_mobile.ui.screens.statistics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.zagrebin.front_mobile.ui.data.RecipeRepository

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentUserId = RecipeRepository.getCurrentUserId()

    StatisticsContent(
        state = state,
        onDayClick = viewModel::selectDay,
        onAddWater = viewModel::addWater,
        onAddMeal = viewModel::addMeal,
        myRecipeOptions = RecipeRepository.getMyPosts(currentUserId),
        savedRecipeOptions = RecipeRepository.getSavedPosts(currentUserId),
        allRecipeOptions = RecipeRepository.getAllPosts()
    )
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun StatisticsScreenPreview() {
    StatisticsContent(
        state = previewStatisticsUiState(),
        onDayClick = {},
        onAddWater = {},
        onAddMeal = { _, _ -> }
    )
}
