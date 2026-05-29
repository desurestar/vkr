package ru.zagrebin.front_mobile.ui.screens.recipe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.data.AppContainer
import ru.zagrebin.front_mobile.domain.model.RecipeDetails
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.components.postCard.RecipeIngredientState
import ru.zagrebin.front_mobile.ui.components.postCard.RecipeStepState
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagState

data class RecipeDetailsUiState(
    val isLoading: Boolean = false,
    val post: PostCardState? = null
)

class RecipeDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val container = AppContainer(application)
    private val details = MutableStateFlow<RecipeDetails?>(null)
    private val isRefreshing = MutableStateFlow(false)
    private val hasLoadError = MutableStateFlow(false)
    private var currentId: Int? = null

    val state: StateFlow<RecipeDetailsUiState> = combine(
        details,
        isRefreshing,
        hasLoadError
    ) { loadedDetails, refreshing, loadError ->
        RecipeDetailsUiState(
            isLoading = currentId != null && loadedDetails == null && refreshing && !loadError,
            post = loadedDetails?.toUi()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipeDetailsUiState())

    fun load(postId: Int) {
        if (currentId == postId) return
        currentId = postId
        details.value = null
        isRefreshing.value = true
        hasLoadError.value = false
        viewModelScope.launch {
            val result = runCatching { container.feedRepository.loadRecipeDetails(postId) }.getOrNull()
            details.value = result?.data
            hasLoadError.value = result == null || (result.isFromCache && result.data == null)
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
