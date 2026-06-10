package ru.zagrebin.front_mobile.ui.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.data.AppContainer
import ru.zagrebin.front_mobile.domain.model.FeedItem
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagState
import ru.zagrebin.front_mobile.ui.navigation.AuthSessionState

data class DraftsState(
    val drafts: List<PostCardState> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class DraftsViewModel(application: Application) : AndroidViewModel(application) {
    private val container = AppContainer(application)
    private val _state = MutableStateFlow(DraftsState(isLoading = true))
    val state: StateFlow<DraftsState> = _state.asStateFlow()

    init {
        loadDrafts()
    }

    fun loadDrafts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val result = container.feedRepository.loadDrafts()
            _state.value = DraftsState(
                drafts = result.data.map { it.toUi() },
                isLoading = false,
                errorMessage = if (result.isFromCache && AuthSessionState.isAuthorized.value) {
                    "Не удалось загрузить черновики с сервера."
                } else {
                    null
                }
            )
        }
    }

    fun deleteDraft(postId: Int) {
        viewModelScope.launch {
            val success = container.feedRepository.deleteDraft(postId)
            if (success) {
                _state.value = _state.value.copy(
                    drafts = _state.value.drafts.filterNot { it.id == postId },
                    errorMessage = null
                )
            } else {
                _state.value = _state.value.copy(errorMessage = "Не удалось удалить черновик.")
            }
        }
    }

    private fun FeedItem.toUi(): PostCardState = PostCardState(
        id = id,
        type = if (time.isBlank() && calories.isBlank()) "ARTICLE" else "RECIPE",
        status = status,
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
        tags = tags.map { TagState(it.id, it.name) }
    )
}
