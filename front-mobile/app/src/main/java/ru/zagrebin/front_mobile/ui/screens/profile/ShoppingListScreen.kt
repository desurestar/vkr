package ru.zagrebin.front_mobile.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ShoppingListScreen(
    onBackClick: () -> Unit,
    viewModel: ShoppingListViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.state.collectAsState()
    var newListName by remember { mutableStateOf("") }
    val expanded = remember { mutableStateMapOf<Long, Boolean>() }
    val itemInputs = remember { mutableStateMapOf<Long, String>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F3F3))
    ) {
        Column {
            ShoppingListTopBar(onBackClick = onBackClick)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    NewShoppingListCard(
                        value = newListName,
                        onValueChange = { newListName = it },
                        onCreateClick = {
                            val name = newListName.trim()
                            if (name.isNotEmpty()) {
                                viewModel.createList(name)
                                newListName = ""
                            }
                        }
                    )
                }

                if (state.isLoading) {
                    item { Text("Загрузка списков...", modifier = Modifier.padding(16.dp)) }
                }

                state.error?.let { message ->
                    item { Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp)) }
                }

                items(state.lists, key = { it.id }) { list ->
                    val isExpanded = expanded[list.id] ?: true
                    ShoppingListCard(
                        list = list,
                        isExpanded = isExpanded,
                        itemInput = itemInputs[list.id].orEmpty(),
                        onToggleExpand = { expanded[list.id] = !isExpanded },
                        onListNameChange = { viewModel.updateList(list.id, it) },
                        onDeleteList = {
                            viewModel.deleteList(list.id)
                            expanded.remove(list.id)
                            itemInputs.remove(list.id)
                        },
                        onItemInputChange = { itemInputs[list.id] = it },
                        onAddItem = { text ->
                            if (text.trim().isNotEmpty()) {
                                viewModel.addItem(list.id, text)
                                itemInputs[list.id] = ""
                            }
                        },
                        onUpdateItem = { itemId, text -> viewModel.updateItem(itemId, text) },
                        onDeleteItem = { itemId -> viewModel.deleteItem(itemId) }
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun ShoppingListTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад"
            )
        }
        Text(
            text = "Список покупок",
            style = MaterialTheme.typography.titleLarge
        )
    }
    HorizontalDivider(color = Color(0xFFE1E1E1))
}

@Composable
private fun NewShoppingListCard(
    value: String,
    onValueChange: (String) -> Unit,
    onCreateClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Новый список",
                style = MaterialTheme.typography.titleMedium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text("Название списка") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF7F5F2),
                        unfocusedContainerColor = Color(0xFFF7F5F2),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onCreateClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF1E7DE),
                        contentColor = Color(0xFF4A4A4A)
                    )
                ) {
                    Text("Создать")
                }
            }
        }
    }
}

@Composable
private fun ShoppingListCard(
    list: ShoppingListUi,
    isExpanded: Boolean,
    itemInput: String,
    onToggleExpand: () -> Unit,
    onListNameChange: (String) -> Unit,
    onDeleteList: () -> Unit,
    onItemInputChange: (String) -> Unit,
    onAddItem: (String) -> Unit,
    onUpdateItem: (Long, String) -> Unit,
    onDeleteItem: (Long) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onToggleExpand() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = list.name,
                        onValueChange = onListNameChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = list.items.size.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8E8E8E)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = null,
                        tint = Color(0xFF8E8E8E)
                    )
                }
                IconButton(onClick = onDeleteList) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить список",
                        tint = Color(0xFF8E8E8E)
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                list.items.forEach { item ->
                    ShoppingItemRow(
                        item = item,
                        onValueChange = { onUpdateItem(item.id, it) },
                        onDeleteClick = { onDeleteItem(item.id) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = itemInput,
                        onValueChange = onItemInputChange,
                        placeholder = { Text("Добавить ингредиент") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF7F5F2),
                            unfocusedContainerColor = Color(0xFFF7F5F2),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onAddItem(itemInput) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C3AED),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Добавить")
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItemUi,
    onValueChange: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF7F5F2),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = item.name,
                onValueChange = onValueChange,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF7F5F2),
                    unfocusedContainerColor = Color(0xFFF7F5F2),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить ингредиент",
                    tint = Color(0xFF8E8E8E)
                )
            }
        }
    }
}

data class ShoppingListUi(
    val id: Long,
    val name: String,
    val items: List<ShoppingItemUi>
)

data class ShoppingItemUi(
    val id: Long,
    val name: String
)

@Preview(showBackground = true, locale = "ru")
@Composable
private fun ShoppingListScreenPreview() {
    ShoppingListScreen(onBackClick = {})
}

