package ru.zagrebin.front_mobile.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileShoppingListBottomSheet(
    lists: SnapshotStateList<ShoppingListUi>,
    selectedListId: Int,
    onSelectedListChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onAddList: (String) -> Unit,
    onDeleteList: (Int) -> Unit,
    onAddItem: (Int, String) -> Unit,
    onUpdateItem: (Int, Int, String) -> Unit,
    onDeleteItem: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isMenuExpanded by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }
    var newItemName by remember { mutableStateOf("") }

    val selectedList = lists.firstOrNull { it.id == selectedListId } ?: lists.firstOrNull()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF7F5F2),
        scrimColor = Color.Black.copy(alpha = 0.32f)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .imePadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .background(Color(0xFFD0D0D0), RoundedCornerShape(2.dp))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExposedDropdownMenuBox(
                    expanded = isMenuExpanded,
                    onExpandedChange = { isMenuExpanded = !isMenuExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    TextField(
                        value = selectedList?.name.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        textStyle = MaterialTheme.typography.titleMedium,
                        trailingIcon = {
                            Icon(
                                imageVector = if (isMenuExpanded) {
                                    Icons.Default.KeyboardArrowUp
                                } else {
                                    Icons.Default.KeyboardArrowDown
                                },
                                contentDescription = null
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF7F5F2),
                            unfocusedContainerColor = Color(0xFFF7F5F2),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        lists.forEach { list ->
                            DropdownMenuItem(
                                text = { Text(list.name) },
                                onClick = {
                                    onSelectedListChange(list.id)
                                    isMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (selectedList != null) {
                    IconButton(onClick = { onDeleteList(selectedList.id) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить список",
                            tint = Color(0xFF8E8E8E)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    placeholder = { Text("Новый список") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val name = newListName.trim()
                        if (name.isNotEmpty()) {
                            onAddList(name)
                            newListName = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF1E7DE),
                        contentColor = Color(0xFF4A4A4A)
                    )
                ) {
                    Text("Создать")
                }
            }

            HorizontalDivider(color = Color(0xFFE1E1E1))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(6.dp)) }
                if (selectedList != null) {
                    items(selectedList.items, key = { it.id }) { item ->
                        ShoppingItemRow(
                            item = item,
                            onValueChange = { updated ->
                                onUpdateItem(selectedList.id, item.id, updated)
                            },
                            onDeleteClick = { onDeleteItem(selectedList.id, item.id) }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(6.dp)) }
            }

            HorizontalDivider(color = Color(0xFFE1E1E1))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    placeholder = { Text("Добавить ингредиент") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val text = newItemName.trim()
                        val target = selectedList
                        if (text.isNotEmpty() && target != null) {
                            onAddItem(target.id, text)
                            newItemName = ""
                        }
                    },
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

@Composable
private fun ShoppingItemRow(
    item: ShoppingItemUi,
    onValueChange: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = item.name,
            onValueChange = onValueChange,
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
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


@Preview(showBackground = true, locale = "ru")
@Composable
private fun ProfileShoppingListBottomSheetPreview() {
    val listA = ShoppingListUi(
        id = 1,
        name = "Токпокки",
        items = androidx.compose.runtime.mutableStateListOf(
            ShoppingItemUi(1, "Рисовые клецки - 500 г"),
            ShoppingItemUi(2, "Паста кочуджан - 2 ст. л.")
        )
    )
    val listB = ShoppingListUi(
        id = 2,
        name = "Мой список",
        items = androidx.compose.runtime.mutableStateListOf(
            ShoppingItemUi(1, "Сыр - 200 г")
        )
    )
    val lists = androidx.compose.runtime.mutableStateListOf(listA, listB)

    ProfileShoppingListBottomSheet(
        lists = lists,
        selectedListId = 1,
        onSelectedListChange = {},
        onDismiss = {},
        onAddList = {},
        onDeleteList = {},
        onAddItem = { _, _ -> },
        onUpdateItem = { _, _, _ -> },
        onDeleteItem = { _, _ -> }
    )
}
