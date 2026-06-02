package ru.zagrebin.front_mobile.ui.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.data.AppContainer
import ru.zagrebin.front_mobile.data.remote.api.ShoppingItemRequest

class ShoppingListViewModel(application: Application) : AndroidViewModel(application) {
    private val api = AppContainer(application).feedApi
    private val _state = MutableStateFlow(ShoppingListState(isLoading = true))
    val state: StateFlow<ShoppingListState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching { api.getShoppingLists() }
                .onSuccess { lists ->
                    _state.value = ShoppingListState(lists = lists.map { it.toUi() })
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message ?: "Не удалось загрузить списки") }
                }
        }
    }

    fun createList(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            runCatching { api.createShoppingList(mapOf("name" to trimmed)) }
                .onSuccess { created ->
                    _state.update { it.copy(lists = it.lists + created.toUi(), error = null) }
                }
                .onFailure { setError(it) }
        }
    }

    fun updateList(listId: Long, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            runCatching { api.updateShoppingList(listId, mapOf("name" to trimmed)) }
                .onSuccess { updated -> replaceList(updated.toUi()) }
                .onFailure { setError(it) }
        }
    }

    fun deleteList(listId: Long) {
        viewModelScope.launch {
            runCatching { api.deleteShoppingList(listId) }
                .onSuccess { _state.update { it.copy(lists = it.lists.filterNot { list -> list.id == listId }, error = null) } }
                .onFailure { setError(it) }
        }
    }

    fun addItem(listId: Long, text: String) {
        val (name, amount) = splitItemText(text)
        if (name.isBlank()) return
        viewModelScope.launch {
            runCatching { api.addShoppingItem(listId, ShoppingItemRequest(name = name, amount = amount)) }
                .onSuccess { created ->
                    _state.update { state ->
                        state.copy(
                            lists = state.lists.map { list ->
                                if (list.id == listId) list.copy(items = list.items + created.toUi()) else list
                            },
                            error = null
                        )
                    }
                }
                .onFailure { setError(it) }
        }
    }

    fun updateItem(itemId: Long, text: String) {
        val (name, amount) = splitItemText(text)
        if (name.isBlank()) return
        viewModelScope.launch {
            runCatching { api.updateShoppingItem(itemId, ShoppingItemRequest(name = name, amount = amount)) }
                .onSuccess { updated ->
                    _state.update { state ->
                        state.copy(
                            lists = state.lists.map { list ->
                                list.copy(items = list.items.map { item -> if (item.id == itemId) updated.toUi() else item })
                            },
                            error = null
                        )
                    }
                }
                .onFailure { setError(it) }
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            runCatching { api.deleteShoppingItem(itemId) }
                .onSuccess {
                    _state.update { state ->
                        state.copy(
                            lists = state.lists.map { list -> list.copy(items = list.items.filterNot { it.id == itemId }) },
                            error = null
                        )
                    }
                }
                .onFailure { setError(it) }
        }
    }

    fun addRecipe(recipeId: Int) {
        viewModelScope.launch {
            runCatching { api.addRecipeToShoppingList(recipeId) }
                .onSuccess { refresh() }
                .onFailure { setError(it) }
        }
    }

    private fun replaceList(list: ShoppingListUi) {
        _state.update { state ->
            state.copy(lists = state.lists.map { if (it.id == list.id) list else it }, error = null)
        }
    }

    private fun setError(error: Throwable) {
        _state.update { it.copy(error = error.message ?: "Не удалось выполнить действие") }
    }
}

data class ShoppingListState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val lists: List<ShoppingListUi> = emptyList()
)

private fun ru.zagrebin.front_mobile.data.remote.api.ShoppingListDto.toUi() = ShoppingListUi(
    id = id,
    name = name,
    items = items.map { it.toUi() }
)

private fun ru.zagrebin.front_mobile.data.remote.api.ShoppingItemDto.toUi() = ShoppingItemUi(
    id = id,
    name = listOf(name, amount.takeIf { it.isNotBlank() }).joinToString(" - ")
)

private fun splitItemText(text: String): Pair<String, String> {
    val parts = text.trim().split(" - ", limit = 2)
    return parts.firstOrNull().orEmpty().trim() to parts.getOrNull(1).orEmpty().trim().ifEmpty { "1" }
}
