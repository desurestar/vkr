package ru.zagrebin.front_mobile.ui.screens.feed

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.data.RecipeRepository

class FeedViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        FeedState(
            posts = RecipeRepository.getPosts()
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
}