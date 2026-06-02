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
    val post: PostCardState? = null,
    val currentUserId: Long? = null
)

class RecipeDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val container = AppContainer(application)
    private val details = MutableStateFlow<RecipeDetails?>(null)
    private val isRefreshing = MutableStateFlow(false)
    private val hasLoadError = MutableStateFlow(false)
    private val currentUserId = MutableStateFlow<Long?>(null)
    private var currentId: Int? = null

    val state: StateFlow<RecipeDetailsUiState> = combine(
        details,
        isRefreshing,
        hasLoadError,
        currentUserId
    ) { loadedDetails, refreshing, loadError, userId ->
        RecipeDetailsUiState(
            isLoading = currentId != null && loadedDetails == null && refreshing && !loadError,
            post = loadedDetails?.toUi(),
            currentUserId = userId
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
            currentUserId.value = container.feedRepository.currentUserId()
            isRefreshing.value = false
        }
    }

    fun addComment(text: String, parentId: Long?) {
        val postId = currentId ?: return
        viewModelScope.launch {
            container.feedRepository.addComment(postId, text, parentId)
            details.value = container.feedRepository.loadRecipeDetails(postId).data
        }
    }

    fun deleteComment(commentId: Long) {
        val postId = currentId ?: return
        viewModelScope.launch {
            container.feedRepository.deleteComment(postId, commentId)
            details.value = container.feedRepository.loadRecipeDetails(postId).data
        }
    }

    private fun RecipeDetails.toUi(): PostCardState = PostCardState(
        id = id,
        authorId = authorId,
        authorName = authorName,
        authorHandle = authorHandle,
        authorAvatarUrl = authorAvatarUrl,
        date = date,
        title = title,
        imageUrl = imageUrl,
        likes = likes,
        isLiked = isLiked,
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
        steps = steps.map { RecipeStepState(it.id, it.title, it.description, it.imageUrl) },
        comments = comments
    )
}
