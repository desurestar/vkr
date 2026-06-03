package ru.zagrebin.front_mobile.ui.screens.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.data.AppContainer
import ru.zagrebin.front_mobile.domain.model.FeedItem
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val appContainer = AppContainer(application)
    private val repository = appContainer.statisticsRepository
    private val feedRepository = appContainer.feedRepository
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val currentUserId = MutableStateFlow<String?>(null)
    private val recentRecipeIds = MutableStateFlow<List<Int>>(emptyList())
    private val recipeSearchState = MutableStateFlow(RecipeSearchState())
    private var lastRecipeSearchQuery: String = ""

    val state: StateFlow<StatisticsUiState> = selectedMonth.flatMapLatest { month ->
        selectedDate.flatMapLatest { date ->
            repository.observeMonth(month, date)
        }
    }.combine(feedRepository.observeRecipes()) { statisticsState, recipes ->
        statisticsState.copy(
            isLoading = false,
            recipeOptions = recipes.map { it.toRecipeMealOption() },
            currentUserId = currentUserId.value,
            recentRecipeIds = recentRecipeIds.value
        )
    }.combine(currentUserId) { statisticsState, userId ->
        statisticsState.copy(currentUserId = userId)
    }.combine(recentRecipeIds) { statisticsState, ids ->
        statisticsState.copy(recentRecipeIds = ids)
    }.combine(recipeSearchState) { statisticsState, searchState ->
        statisticsState.copy(
            recipeSearchResults = searchState.results,
            isRecipeSearchLoading = searchState.isLoading,
            hasMoreRecipeSearchResults = searchState.hasMore
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState(isLoading = true))

    init {
        refresh()
        refreshRecipes()
        viewModelScope.launch { currentUserId.value = feedRepository.currentUserId()?.toString() }
    }

    fun selectDay(dayId: Int) {
        val date = LocalDate.ofEpochDay(dayId.toLong())
        selectedDate.value = date
        val month = YearMonth.from(date)
        if (selectedMonth.value != month) selectedMonth.value = month
    }

    fun showPreviousMonth() {
        val month = selectedMonth.value.minusMonths(1)
        selectedMonth.value = month
        selectedDate.value = month.atDay(month.lengthOfMonth())
        refresh()
    }

    fun showNextMonth() {
        val month = selectedMonth.value.plusMonths(1)
        selectedMonth.value = month
        selectedDate.value = if (month == YearMonth.now()) LocalDate.now() else month.atDay(1)
        refresh()
    }

    fun addWater(amountMl: Int = 250) {
        viewModelScope.launch { repository.addWater(selectedDate.value, amountMl) }
    }

    fun addMeal(type: MealType, draft: MealDraft) {
        draft.recipeId?.let { recipeId ->
            recentRecipeIds.value = (listOf(recipeId) + recentRecipeIds.value.filterNot { it == recipeId }).take(10)
        }
        viewModelScope.launch { repository.addMeal(selectedDate.value, type, draft) }
    }

    fun updateSettings(settings: StatisticsSettings) {
        viewModelScope.launch { repository.updateSettings(settings) }
    }

    fun searchRecipes(query: String) {
        lastRecipeSearchQuery = query
        recipeSearchState.value = RecipeSearchState(isLoading = true)
        viewModelScope.launch { loadRecipeSearchPage(query, page = 0, append = false) }
    }

    fun loadMoreRecipes() {
        val current = recipeSearchState.value
        if (current.isLoading || !current.hasMore) return
        recipeSearchState.value = current.copy(isLoading = true)
        viewModelScope.launch { loadRecipeSearchPage(lastRecipeSearchQuery, current.page + 1, append = true) }
    }

    fun refresh() {
        viewModelScope.launch { repository.refreshMonth(selectedMonth.value) }
    }

    private fun refreshRecipes() {
        viewModelScope.launch { feedRepository.loadRecipes() }
    }

    private suspend fun loadRecipeSearchPage(query: String, page: Int, append: Boolean) {
        val result = feedRepository.searchRecipes(query, page, RECIPE_SEARCH_PAGE_SIZE)
        if (query != lastRecipeSearchQuery) return
        val items = result.data.map { it.toRecipeMealOption() }
        val current = recipeSearchState.value
        recipeSearchState.value = RecipeSearchState(
            results = if (append) (current.results + items).distinctBy { it.id } else items,
            page = page,
            isLoading = false,
            hasMore = !result.isFromCache && items.size >= RECIPE_SEARCH_PAGE_SIZE
        )
    }

    fun addMockMeal(type: MealType) = Unit
}

private const val RECIPE_SEARCH_PAGE_SIZE = 10

private data class RecipeSearchState(
    val results: List<RecipeMealOption> = emptyList(),
    val page: Int = 0,
    val isLoading: Boolean = false,
    val hasMore: Boolean = false
)

private fun FeedItem.toRecipeMealOption() = RecipeMealOption(
    id = id,
    authorId = authorId,
    authorName = authorName,
    date = date,
    title = title,
    calories = calories,
    isSaved = isSaved || isLiked,
    proteinsPer100 = proteinsPer100,
    fatsPer100 = fatsPer100,
    carbsPer100 = carbsPer100,
    kcalPer100 = kcalPer100,
    tags = tags.map { it.name }
)
