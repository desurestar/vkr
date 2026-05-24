package ru.zagrebin.front_mobile.ui.screens.recipe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val container = AppContainer(application)
    private val currentId = MutableStateFlow<Int?>(null)

    val state: StateFlow<RecipeDetailsUiState> = currentId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else container.observeRecipeDetailsUseCase(id)
        }
        .map { details ->
            val hasRequest = currentId.value != null
            RecipeDetailsUiState(
                isLoading = hasRequest && details == null,
                post = details?.toUi()
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipeDetailsUiState())

    fun load(postId: Int) {
        if (currentId.value == postId) return
        currentId.value = postId
        viewModelScope.launch {
            runCatching { container.refreshRecipeDetailsUseCase(postId) }
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
