package ru.zagrebin.front_mobile.ui.screens.recipe

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.zagrebin.front_mobile.ui.theme.AppPageBackgroundColor
import ru.zagrebin.front_mobile.ui.theme.LightPrimary

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    onBackClick: () -> Unit = {}
) {
    var postTitle by rememberSaveable { mutableStateOf("") }
    var dishTitle by rememberSaveable { mutableStateOf("") }
    val selectedTags = remember { mutableStateListOf<String>() }
    val ingredients = remember { mutableStateListOf<IngredientDraft>() }
    var showTagSheet by rememberSaveable { mutableStateOf(false) }
    var showIngredientSheet by rememberSaveable { mutableStateOf(false) }

    if (showTagSheet) {
        TagPickBottomSheet(
            tags = listOf("Завтрак", "Обед", "Ужин", "ПП", "Веган"),
            selected = selectedTags,
            onDismiss = { showTagSheet = false },
            onAddClick = { showTagSheet = false }
        )
    }

    if (showIngredientSheet) {
        IngredientAddBottomSheet(
            onDismiss = { showIngredientSheet = false },
            onAddClick = { draft ->
                ingredients.add(draft)
                showIngredientSheet = false
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppPageBackgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            CreateRecipeTopBar(onBackClick = onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Заголовок поста",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6F6F6F)
                )
                TextField(
                    value = postTitle,
                    onValueChange = { postTitle = it },
                    placeholder = { Text("Заголовок поста...") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = inputColors()
                )

                PhotoPlaceholder()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showTagSheet = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Добавить тег")
                    }
                    TagRow(tags = selectedTags)
                }

                Text(
                    text = "Описание рецепта",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                TextField(
                    value = dishTitle,
                    onValueChange = { dishTitle = it },
                    placeholder = { Text("Название блюда...") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = inputColors()
                )

                IngredientsBlock(
                    ingredients = ingredients,
                    onAddClick = { showIngredientSheet = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Черновик")
                    }
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF6C166)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Опубликовать", color = Color(0xFF1E1C1F))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun CreateRecipeTopBar(onBackClick: () -> Unit) {
    Surface(color = AppPageBackgroundColor) {
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
                text = "Создать пост",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun PhotoPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color(0xFFE3E3E3), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = Color(0xFF7A7A7A),
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(text = "Добавить фото", color = Color(0xFF7A7A7A))
        }
    }
}

@Composable
private fun TagRow(tags: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        tags.forEach { tag ->
            Surface(
                color = Color(0xFFEDE6DE),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = tag,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun IngredientsBlock(
    ingredients: List<IngredientDraft>,
    onAddClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ингредиенты",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Добавить")
            }
        }
        ingredients.forEach { ingredient ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = ingredient.name, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${ingredient.amount} ${ingredient.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7A7A7A)
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun TagPickBottomSheet(
    tags: List<String>,
    selected: MutableList<String>,
    onDismiss: () -> Unit,
    onAddClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF7F5F2),
        scrimColor = Color.Black.copy(alpha = 0.32f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Выберите теги",
                style = MaterialTheme.typography.titleMedium
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.forEach { tag ->
                    val checked = selected.contains(tag)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { value ->
                                if (value) {
                                    if (!selected.contains(tag)) selected.add(tag)
                                } else {
                                    selected.remove(tag)
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = LightPrimary)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = tag, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Добавить", color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun IngredientAddBottomSheet(
    onDismiss: () -> Unit,
    onAddClick: (IngredientDraft) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var unit by rememberSaveable { mutableStateOf("г") }
    var expanded by remember { mutableStateOf(false) }

    val units = listOf("г", "мл", "шт", "ст. л.")
    val amountValue = amount.toFloatOrNull() ?: 0f
    val canSubmit = name.trim().isNotEmpty() && amountValue > 0f

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF7F5F2),
        scrimColor = Color.Black.copy(alpha = 0.32f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Добавить ингредиент",
                style = MaterialTheme.typography.titleMedium
            )

            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Название") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = inputColors()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = amount,
                    onValueChange = { amount = it.replace(',', '.').filter { ch -> ch.isDigit() || ch == '.' } },
                    placeholder = { Text("Количество") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = inputColors()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .weight(1f)
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = inputColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        units.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    unit = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (!canSubmit) return@Button
                    onAddClick(
                        IngredientDraft(
                            name = name.trim(),
                            amount = amountValue,
                            unit = unit
                        )
                    )
                },
                enabled = canSubmit,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Добавить", color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun inputColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent
)

data class IngredientDraft(
    val name: String,
    val amount: Float,
    val unit: String
)

@Preview(showBackground = true, locale = "ru")
@Composable
private fun CreateRecipeScreenPreview() {
    CreateRecipeScreen()
}
