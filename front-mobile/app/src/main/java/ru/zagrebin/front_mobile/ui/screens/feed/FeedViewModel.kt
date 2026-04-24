package ru.zagrebin.front_mobile.ui.screens.feed

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.components.postCard.RecipeIngredientState
import ru.zagrebin.front_mobile.ui.components.postCard.RecipeStepState
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagState

class FeedViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        FeedState(
            posts = mockPosts()
        )
    )

    val state: StateFlow<FeedState> = _state

    fun onSearch(query: String) {
        _state.update { current ->
            val updatedPosts = current.posts.map { post ->
                post.copy(
                    tags = post.tags.map { tag ->
                        tag.copy(
                            isHighlighted = tag.title.contains(query, true)
                        )
                    }
                )
            }

            current.copy(
                searchQuery = query,
                posts = updatedPosts
            )
        }
    }

    fun onTagClick(postId: Int, tagId: Int) {
        _state.update { current ->
            current.copy(
                posts = current.posts.map { post ->
                    if (post.id == postId) {
                        post.copy(
                            tags = post.tags.map {
                                if (it.id == tagId)
                                    it.copy(isHighlighted = !it.isHighlighted)
                                else it
                            }
                        )
                    } else post
                }
            )
        }
    }

    fun getPostById(postId: Int): PostCardState? {
        return _state.value.posts.firstOrNull { it.id == postId }
    }

    private fun mockPosts(): List<PostCardState> {
        return List(5) { index ->
            PostCardState(
                id = index,
                authorId = "42",
                authorName = "Дмитрий Загребин",
                authorHandle = "@Dima123",
                date = "24.03.2026",
                title = "Токпокки (Tteokbokki) — классический рецепт",
                imageUrl = "https://img.freepik.com/premium-photo/salad-with-mixed-seafood-dark-plate_102375-5144.jpg?semt=ais_hybrid&w=740",
                likes = "38.6k",
                time = "35 мин",
                calories = "250 ккал",
                views = "53.7k",
                tags = listOf(
                    TagState(1, "#tteokbokki"),
                    TagState(2, "#корея"),
                    TagState(3, "#острое"),
                    TagState(4, "#streetfood")
                ),
                ingredients = listOf(
                    RecipeIngredientState("Творог - 500 г"),
                    RecipeIngredientState("Яйцо - 1 шт"),
                    RecipeIngredientState("Голубика - 150 г"),
                    RecipeIngredientState("Сахар - 2 ст. л."),
                    RecipeIngredientState("Мука - 3-4 ст. л."),
                    RecipeIngredientState("Разрыхлитель - 0,5 ч. л."),
                    RecipeIngredientState("Ванилин - 1 щепотка")
                ),
                steps = listOf(
                    RecipeStepState(
                        id = 1,
                        title = "Шаг 1",
                        description = "Подготовьте ингредиенты и обсушите ягоды бумажным полотенцем.",
                        imageUrl = "https://img.freepik.com/premium-photo/ingredients-cooking_23-2148824466.jpg"
                    ),
                    RecipeStepState(
                        id = 2,
                        title = "Шаг 2",
                        description = "Смешайте творог, яйцо, сахар и ванилин до однородной массы.",
                        imageUrl = "https://img.freepik.com/free-photo/cottage-cheese-bowl_114579-10256.jpg"
                    ),
                    RecipeStepState(
                        id = 3,
                        title = "Шаг 3",
                        description = "Добавьте муку, разрыхлитель и аккуратно вмешайте голубику.",
                        imageUrl = null
                    ),
                    RecipeStepState(
                        id = 4,
                        title = "Шаг 4",
                        description = "Сформируйте сырники и обжарьте на среднем огне до румяной корочки.",
                        imageUrl = "https://img.freepik.com/free-photo/pancakes-pan_23-2148743818.jpg"
                    )
                )
            )
        }
    }
}