package ru.zagrebin.front_mobile.ui.screens.recipe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.data.AppContainer
import ru.zagrebin.front_mobile.data.remote.api.ShoppingItemRequest
import ru.zagrebin.front_mobile.domain.model.RecipeDetails
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.components.postCard.RecipeIngredientState
import ru.zagrebin.front_mobile.ui.components.postCard.RecipeStepState
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagState
import ru.zagrebin.front_mobile.ui.screens.statistics.MealDraft
import ru.zagrebin.front_mobile.ui.screens.statistics.MealType
import ru.zagrebin.front_mobile.ui.navigation.AuthSessionState
import ru.zagrebin.front_mobile.ui.screens.profile.GuestShoppingListStore
import java.time.LocalDate

data class RecipeDetailsUiState(
    val isLoading: Boolean = false,
    val post: PostCardState? = null,
    val currentUserId: Long? = null
)

class RecipeDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val container = AppContainer(application)
    private val guestShoppingListStore = GuestShoppingListStore(application)
    private val details = MutableStateFlow<RecipeDetails?>(null)
    private val isRefreshing = MutableStateFlow(false)
    private val hasLoadError = MutableStateFlow(false)
    private val currentUserId = MutableStateFlow<Long?>(null)
    private var currentId: Int? = null
    private var viewTrackingJob: Job? = null

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
            if (result?.data != null) {
                scheduleViewTracking(postId)
            }
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

    fun addMeal(type: MealType, draft: MealDraft) {
        val postId = currentId ?: return
        viewModelScope.launch {
            container.statisticsRepository.addRecipeMeal(LocalDate.now(), type, postId, draft)
        }
    }

    fun addIngredientsToShoppingList(ingredients: List<String>) {
        val post = details.value ?: return
        if (ingredients.isEmpty()) return
        viewModelScope.launch {
            if (!AuthSessionState.isAuthorized.value) {
                guestShoppingListStore.createRecipeList(post.title, ingredients)
                return@launch
            }
            runCatching {
                val list = container.feedApi.createShoppingList(mapOf("name" to post.title))
                ingredients.forEach { ingredient ->
                    val (name, amount) = ingredient.splitShoppingText()
                    container.feedApi.addShoppingItem(list.id, ShoppingItemRequest(name = name, amount = amount))
                }
            }
        }
    }


    private fun scheduleViewTracking(postId: Int) {
        viewTrackingJob?.cancel()
        viewTrackingJob = viewModelScope.launch {
            delay(VIEW_TRACKING_DELAY_MS)
            if (currentId == postId && container.feedRepository.recordPostView(postId, TYPE_RECIPE, VIEW_TRACKING_DURATION_SECONDS)) {
                details.value = container.feedRepository.loadRecipeDetails(postId).data
            }
        }
    }

    override fun onCleared() {
        viewTrackingJob?.cancel()
        super.onCleared()
    }

    private fun String.splitShoppingText(): Pair<String, String> {
        val parts = trim().split(" - ", limit = 2)
        return parts.firstOrNull().orEmpty().trim() to parts.getOrNull(1).orEmpty().trim().ifEmpty { "1" }
    }

    private fun RecipeDetails.toUi(): PostCardState = PostCardState(
        id = id,
        status = status,
        authorId = authorId,
        authorName = authorName,
        authorHandle = authorHandle,
        authorAvatarUrl = authorAvatarUrl,
        date = date,
        title = title,
        description = description,
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

    private companion object {
        const val TYPE_RECIPE = "recipe"
        const val VIEW_TRACKING_DELAY_MS = 8_000L
        const val VIEW_TRACKING_DURATION_SECONDS = 8
    }
}
