package ru.zagrebin.front_mobile.ui.components.postCard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagState

class PostCardViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        PostCardState(
            id = 1,
            authorName = "Дмитрий Загребин",
            authorHandle = "@Dima123",
            date = "24.03.2026",
            title = "Токпокки (Tteokbokki) — классический рецепт",
            imageUrl = "https://images.unsplash.com/photo-1633436374961-09f349d3b1c9",
            likes = "38.6k",
            time = "35 мин",
            calories = "250 ккал",
            views = "53.7k",
            tags = listOf(
                TagState(1, "#tteokbokki"),
                TagState(2, "#корея"),
                TagState(3, "#острое"),
                TagState(4, "#streetfood")
            )
        )
    )

    val state: StateFlow<PostCardState> = _state

    fun onTagClick(tagId: Int) {
        _state.update { current ->
            current.copy(
                tags = current.tags.map {
                    if (it.id == tagId)
                        it.copy(isHighlighted = !it.isHighlighted)
                    else it
                }
            )
        }
    }

    fun highlightBySearch(query: String) {
        _state.update { current ->
            current.copy(
                tags = current.tags.map {
                    it.copy(
                        isHighlighted = it.title.contains(query, ignoreCase = true)
                    )
                }
            )
        }
    }
}