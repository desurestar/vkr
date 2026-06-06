package ru.zagrebin.front_mobile.ui.screens.profile

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class GuestShoppingListStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLists(): List<ShoppingListUi> = readLists()

    fun createList(name: String): ShoppingListUi {
        val list = ShoppingListUi(id = nextId(KEY_NEXT_LIST_ID), name = name, items = emptyList())
        writeLists(readLists() + list)
        return list
    }

    fun updateList(listId: Long, name: String): ShoppingListUi? {
        var updated: ShoppingListUi? = null
        writeLists(readLists().map { list ->
            if (list.id == listId) {
                list.copy(name = name).also { updated = it }
            } else {
                list
            }
        })
        return updated
    }

    fun deleteList(listId: Long) {
        writeLists(readLists().filterNot { it.id == listId })
    }

    fun addItem(listId: Long, text: String): ShoppingItemUi? {
        val item = ShoppingItemUi(id = nextId(KEY_NEXT_ITEM_ID), name = text)
        var added = false
        writeLists(readLists().map { list ->
            if (list.id == listId) {
                added = true
                list.copy(items = list.items + item)
            } else {
                list
            }
        })
        return item.takeIf { added }
    }

    fun updateItem(itemId: Long, text: String): ShoppingItemUi? {
        var updated: ShoppingItemUi? = null
        writeLists(readLists().map { list ->
            list.copy(
                items = list.items.map { item ->
                    if (item.id == itemId) {
                        item.copy(name = text).also { updated = it }
                    } else {
                        item
                    }
                }
            )
        })
        return updated
    }

    fun deleteItem(itemId: Long) {
        writeLists(readLists().map { list ->
            list.copy(items = list.items.filterNot { it.id == itemId })
        })
    }

    fun createRecipeList(name: String, ingredients: List<String>) {
        val list = createList(name)
        ingredients.forEach { ingredient -> addItem(list.id, ingredient) }
    }

    private fun readLists(): List<ShoppingListUi> {
        val raw = prefs.getString(KEY_LISTS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val listObject = array.getJSONObject(index)
                val itemArray = listObject.optJSONArray("items") ?: JSONArray()
                ShoppingListUi(
                    id = listObject.getLong("id"),
                    name = listObject.optString("name"),
                    items = List(itemArray.length()) { itemIndex ->
                        val itemObject = itemArray.getJSONObject(itemIndex)
                        ShoppingItemUi(
                            id = itemObject.getLong("id"),
                            name = itemObject.optString("name")
                        )
                    }
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun writeLists(lists: List<ShoppingListUi>) {
        val array = JSONArray()
        lists.forEach { list ->
            val itemArray = JSONArray()
            list.items.forEach { item ->
                itemArray.put(
                    JSONObject()
                        .put("id", item.id)
                        .put("name", item.name)
                )
            }
            array.put(
                JSONObject()
                    .put("id", list.id)
                    .put("name", list.name)
                    .put("items", itemArray)
            )
        }
        prefs.edit().putString(KEY_LISTS, array.toString()).apply()
    }

    private fun nextId(key: String): Long {
        val current = prefs.getLong(key, -1L)
        prefs.edit().putLong(key, current - 1).apply()
        return current
    }

    private companion object {
        const val PREFS_NAME = "guest_shopping_lists"
        const val KEY_LISTS = "lists"
        const val KEY_NEXT_LIST_ID = "next_list_id"
        const val KEY_NEXT_ITEM_ID = "next_item_id"
    }
}
