package ru.zagrebin.front_mobile.ui.screens.recipe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.data.AppContainer
import ru.zagrebin.front_mobile.data.repository.RefreshResult
import ru.zagrebin.front_mobile.domain.model.RecipeDetails
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.components.postCard.RecipeIngredientState
import ru.zagrebin.front_mobile.ui.components.postCard.RecipeStepState
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagState

data class RecipeDetailsUiState(
    val isLoading: Boolean = false,
    val post: PostCardState? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val container = AppContainer(application)
    private val currentId = MutableStateFlow<Int?>(null)
    private val isRefreshing = MutableStateFlow(false)
    private val hasLoadError = MutableStateFlow(false)
    private val canShowCache = MutableStateFlow(false)

    val state: StateFlow<RecipeDetailsUiState> = currentId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else container.observeRecipeDetailsUseCase(id)
        }
        .combine(isRefreshing) { details, refreshing -> details to refreshing }
        .combine(hasLoadError) { (details, refreshing), loadError -> Triple(details, refreshing, loadError) }
        .combine(canShowCache) { (details, refreshing, loadError), showCache ->
            val hasRequest = currentId.value != null
            val visibleDetails = if (showCache) details else null
            val shouldShowLoading = hasRequest && visibleDetails == null && refreshing && !loadError
            RecipeDetailsUiState(
                isLoading = shouldShowLoading,
                post = visibleDetails?.toUi()
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipeDetailsUiState())

    fun load(postId: Int) {
        if (currentId.value == postId) return
        currentId.value = postId
        isRefreshing.value = true
        hasLoadError.value = false
        canShowCache.value = false
        viewModelScope.launch {
            val result = runCatching { container.refreshRecipeDetailsUseCase(postId) }
                .getOrDefault(RefreshResult.Fallback)
            hasLoadError.value = result == RefreshResult.Fallback
            canShowCache.value = true
            isRefreshing.value = false
        }
    }

    private fun RecipeDetails.toUi(): PostCardState = PostCardState(
        id = id,
        authorId = authorId,
        authorName = authorName,
        authorHandle = authorHandle,
        date = date,
        title = title,
        imageUrl = imageUrl,
        likes = likes,
        time = time,
        calories = calories,
        views = views,
        isSaved = isSaved,
        proteinsPer100 = proteinsPer100,
        fatsPer100 = fatsPer100,
        carbsPer100 = carbsPer100,
        kcalPer100 = kcalPer100,
        tags = tags.map { TagState(it.id, it.name) },
        ingredients = ingredients.map { RecipeIngredientState(it.text) },
        steps = steps.map { RecipeStepState(it.id, it.title, it.description, it.imageUrl) }
    )
}
