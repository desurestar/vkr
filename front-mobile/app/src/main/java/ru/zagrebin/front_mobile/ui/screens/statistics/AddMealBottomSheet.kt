package ru.zagrebin.front_mobile.ui.screens.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.compose.material3.MenuAnchorType
import kotlin.math.roundToInt

private enum class RecipeSource(val title: String) {
    MY("Мои"),
    SAVED("Сохраненные"),
    ALL("Все"),
    RECENT("Недавние")
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AddMealBottomSheet(
    mealType: MealType,
    onDismiss: () -> Unit,
    onAddClick: (MealType, MealDraft) -> Unit,
    recipeOptions: List<RecipeMealOption> = emptyList(),
    currentUserId: String? = null,
    recentRecipeIds: List<Int> = emptyList(),
    allowMealTypeSelection: Boolean = false,
    initialDraft: MealDraft? = null
) {
    var selectedMealType by rememberSaveable(mealType, allowMealTypeSelection) {
        mutableStateOf(mealType)
    }
    val initialPortion = initialDraft?.portionGrams?.takeIf { it > 0 }?.toString() ?: "0"
    var title by rememberSaveable(mealType, initialDraft) { mutableStateOf(initialDraft?.title.orEmpty()) }
    var portion by rememberSaveable(mealType, initialDraft) { mutableStateOf(initialPortion) }
    var proteins100 by rememberSaveable(mealType, initialDraft) {
        mutableStateOf(initialDraft?.proteinsPer100?.pretty() ?: "0")
    }
    var fats100 by rememberSaveable(mealType, initialDraft) {
        mutableStateOf(initialDraft?.fatsPer100?.pretty() ?: "0")
    }
    var carbs100 by rememberSaveable(mealType, initialDraft) {
        mutableStateOf(initialDraft?.carbsPer100?.pretty() ?: "0")
    }
    var kcal100 by rememberSaveable(mealType, initialDraft) {
        mutableStateOf(initialDraft?.kcalPer100?.toString() ?: "0")
    }
    val myRecipeOptions = remember(recipeOptions, currentUserId) {
        recipeOptions.filter { option -> currentUserId != null && option.authorId == currentUserId }
    }
    val savedRecipeOptions = remember(recipeOptions) { recipeOptions.filter { it.isSaved } }
    val allRecipeOptions = recipeOptions
    val hasRecipeSources = recipeOptions.isNotEmpty()
    val initialRecipeSource = when {
        myRecipeOptions.isNotEmpty() -> RecipeSource.MY
        savedRecipeOptions.isNotEmpty() -> RecipeSource.SAVED
        else -> RecipeSource.ALL
    }
    var selectedRecipeSource by rememberSaveable(initialRecipeSource) { mutableStateOf(initialRecipeSource) }
    var isRecipeMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var recipeQuery by rememberSaveable(initialDraft) { mutableStateOf(initialDraft?.title.orEmpty()) }
    var recipeFieldSize by remember { mutableStateOf(IntSize.Zero) }
    val recipeFocusRequester = remember { FocusRequester() }
    var selectedRecipeId by rememberSaveable(initialDraft) { mutableStateOf(initialDraft?.recipeId) }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    var isMealTypeMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val recentRecipeOptions = remember(recentRecipeIds, allRecipeOptions) {
        val recipesById = allRecipeOptions.associateBy { it.id }
        recentRecipeIds.mapNotNull { recipesById[it] }
    }
    val hasRecentRecipes = recentRecipeOptions.isNotEmpty()

    val sourceRecipes = when (selectedRecipeSource) {
        RecipeSource.MY -> myRecipeOptions
        RecipeSource.SAVED -> savedRecipeOptions
        RecipeSource.ALL -> allRecipeOptions
        RecipeSource.RECENT -> if (hasRecentRecipes) recentRecipeOptions else allRecipeOptions
    }
    val filteredRecipes = remember(sourceRecipes, recipeQuery, selectedRecipeSource, recentRecipeIds) {
        sourceRecipes.searchAndSortRecipes(recipeQuery, selectedRecipeSource, recentRecipeIds)
    }

    val portionValue = portion.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val proteins100Value = proteins100.replace(',', '.').toFloatOrNull() ?: 0f
    val fats100Value = fats100.replace(',', '.').toFloatOrNull() ?: 0f
    val carbs100Value = carbs100.replace(',', '.').toFloatOrNull() ?: 0f
    val kcal100Value = kcal100.toIntOrNull() ?: 0
    val factor = if (portionValue > 0) portionValue / 100f else 0f
    val totalProteins = proteins100Value * factor
    val totalFats = fats100Value * factor
    val totalCarbs = carbs100Value * factor
    val totalKcal = (kcal100Value * factor).roundToInt()
    val isTitleValid = title.trim().isNotEmpty()
    val isPortionValid = portionValue > 0
    val canSubmit = isTitleValid && isPortionValid
    val accentColor = Color(0xFFF59B5A)
    val dropdownBackground = Color(0xFFF7F2ED)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFEFEFEF),
        scrimColor = Color.Black.copy(alpha = 0.32f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (allowMealTypeSelection) {
                Text(
                    text = "Прием пищи",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                ExposedDropdownMenuBox(
                    expanded = isMealTypeMenuExpanded,
                    onExpandedChange = { isMealTypeMenuExpanded = !isMealTypeMenuExpanded }
                ) {
                    TextField(
                        value = selectedMealType.title,
                        onValueChange = {},
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMealTypeMenuExpanded) },
                        colors = inputColors()
                    )
                    DropdownMenu(
                        expanded = isMealTypeMenuExpanded,
                        onDismissRequest = { isMealTypeMenuExpanded = false }
                    ) {
                        MealType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.title) },
                                onClick = {
                                    selectedMealType = type
                                    isMealTypeMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (hasRecipeSources) {
                Text(
                    text = "Из рецептов",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                RecipeSourceRow(
                    selected = selectedRecipeSource,
                    hasMy = myRecipeOptions.isNotEmpty(),
                    hasSaved = savedRecipeOptions.isNotEmpty(),
                    hasAll = allRecipeOptions.isNotEmpty(),
                    hasRecent = hasRecentRecipes,
                    onSelected = { selectedRecipeSource = it }
                )
                ExposedDropdownMenuBox(
                    expanded = isRecipeMenuExpanded,
                    onExpandedChange = { expanded ->
                        isRecipeMenuExpanded = expanded
                        if (expanded) {
                            recipeFocusRequester.requestFocus()
                        }
                    }
                ) {
                    TextField(
                        value = recipeQuery,
                        onValueChange = {
                            recipeQuery = it
                            selectedRecipeId = null
                            if (!isRecipeMenuExpanded) {
                                isRecipeMenuExpanded = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable)
                            .focusRequester(recipeFocusRequester)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    isRecipeMenuExpanded = true
                                }
                            }
                            .onGloballyPositioned { layoutCoordinates ->
                                recipeFieldSize = layoutCoordinates.size
                            },
                        placeholder = {
                            Text("Выберете блюдо")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        readOnly = false,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRecipeMenuExpanded) },
                        colors = inputColors()
                    )
                    DropdownMenu(
                        expanded = isRecipeMenuExpanded,
                        onDismissRequest = { isRecipeMenuExpanded = false },
                        properties = PopupProperties(focusable = false),
                        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                        containerColor = dropdownBackground,
                        modifier = Modifier
                            .width(with(androidx.compose.ui.platform.LocalDensity.current) {
                                recipeFieldSize.width.toDp()
                            })
                    ) {
                        if (filteredRecipes.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Ничего не найдено") },
                                onClick = {}
                            )
                        } else {
                            filteredRecipes.forEachIndexed { index, recipe ->
                                val shape = when (index) {
                                    filteredRecipes.lastIndex -> RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                                    else -> RoundedCornerShape(0.dp)
                                }
                                Surface(
                                    shape = shape,
                                    color = Color.White,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(text = highlightQuery(recipe.title, recipeQuery, accentColor))
                                        },
                                        onClick = {
                                            val selectedTitle = recipe.title
                                            recipeQuery = selectedTitle
                                            title = selectedTitle
                                            proteins100 = recipe.proteinsPer100.pretty()
                                            fats100 = recipe.fatsPer100.pretty()
                                            carbs100 = recipe.carbsPer100.pretty()
                                            kcal100 = recipe.kcalPer100.toString()
                                            selectedRecipeId = recipe.id
                                            isRecipeMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Название",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFA4A4A4),
                    modifier = Modifier.weight(1f)
                )
            }

            TextField(
                value = title,
                onValueChange = {
                    title = it
                    selectedRecipeId = null
                },
                placeholder = { Text("Введите название") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = showErrors && !isTitleValid,
                colors = inputColors()
            )
            if (showErrors && !isTitleValid) {
                Text(
                    text = "Введите название блюда",
                    color = Color(0xFFD32F2F),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "Нутриенты",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2A2A2A)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KbjuField(
                    title = "Белки",
                    value = proteins100,
                    onValueChange = { proteins100 = it },
                    modifier = Modifier.weight(1f)
                )
                KbjuField(
                    title = "Жиры",
                    value = fats100,
                    onValueChange = { fats100 = it },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KbjuField(
                    title = "Углеводы",
                    value = carbs100,
                    onValueChange = { carbs100 = it },
                    modifier = Modifier.weight(1f)
                )
                KbjuField(
                    title = "Ккал",
                    value = kcal100,
                    onValueChange = { kcal100 = it.filter { ch -> ch.isDigit() } },
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
            }

            Text("Порция гр", color = Color(0xFF666666), style = MaterialTheme.typography.bodyMedium)
            TextField(
                value = portion,
                onValueChange = { portion = it.filter { ch -> ch.isDigit() } },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                keyboardActions = KeyboardActions(onDone = { showErrors = true }),
                isError = showErrors && !isPortionValid,
                colors = inputColors()
            )
            PortionQuickActions(
                portionValue = portionValue,
                onPortionChange = { newValue -> portion = newValue.toString() }
            )
            if (showErrors && !isPortionValid) {
                Text(
                    text = "Укажите порцию больше 0",
                    color = Color(0xFFD32F2F),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            PortionTotalsCompact(
                portionGrams = portionValue,
                kcal = totalKcal,
                proteins = totalProteins,
                fats = totalFats,
                carbs = totalCarbs,
                accentColor = accentColor
            )

            Surface(
                onClick = {
                    showErrors = true
                    if (!canSubmit) return@Surface
                     val draft = MealDraft(
                         title = title.trim(),
                         portionGrams = portionValue,
                         isLiquid = false,
                         proteinsPer100 = proteins100Value,
                         fatsPer100 = fats100Value,
                         carbsPer100 = carbs100Value,
                         kcalPer100 = kcal100Value,
                         recipeId = selectedRecipeId
                     )
                     onAddClick(selectedMealType, draft)
                 },
                 shape = RoundedCornerShape(12.dp),
                color = if (canSubmit) Color(0xFF7C3AED) else Color(0xFFBDBDBD),
                 modifier = Modifier
                     .fillMaxWidth()
                     .padding(top = 16.dp, bottom = 32.dp)
             ) {
                 Text(
                     text = "Добавить в ${selectedMealType.title}",
                     modifier = Modifier.padding(vertical = 14.dp),
                     color = Color.White,
                     fontWeight = FontWeight.Bold,
                     style = MaterialTheme.typography.titleMedium,
                     textAlign = androidx.compose.ui.text.style.TextAlign.Center
                 )
             }
         }
     }
 }

@Composable
fun KbjuField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    supportingText: String = "на 100 г"
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.bodySmall, color = Color(0xFF666666))
        TextField(
            value = value,
            onValueChange = onValueChange,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            supportingText = { Text(text = supportingText, color = Color(0xFF8A8A8A)) },
            colors = inputColors()
        )
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun KbjuFieldPreview() {
    KbjuField(title = "Белки на 100 гр", value = "10.5", onValueChange = {})
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun AddMealBottomSheetTriggerPreview() {
    Surface(color = Color(0xFFEFEFEF), modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "BottomSheet открывается по клику '+'",
            modifier = Modifier.padding(16.dp),
            color = Color(0xFF666666)
        )
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun AddMealBottomSheetPreview() {
    AddMealBottomSheet(
        mealType = MealType.BREAKFAST,
        onDismiss = {},
        onAddClick = { _, _ -> }
    )
}

@Composable
private fun RecipeSourceRow(
    selected: RecipeSource,
    hasMy: Boolean,
    hasSaved: Boolean,
    hasAll: Boolean,
    hasRecent: Boolean,
    onSelected: (RecipeSource) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RecipeSourceChip(
            title = RecipeSource.MY.title,
            selected = selected == RecipeSource.MY,
            enabled = hasMy,
            onClick = { onSelected(RecipeSource.MY) }
        )
        RecipeSourceChip(
            title = RecipeSource.SAVED.title,
            selected = selected == RecipeSource.SAVED,
            enabled = hasSaved,
            onClick = { onSelected(RecipeSource.SAVED) }
        )
        RecipeSourceChip(
            title = RecipeSource.ALL.title,
            selected = selected == RecipeSource.ALL,
            enabled = hasAll,
            onClick = { onSelected(RecipeSource.ALL) }
        )
        RecipeSourceChip(
            title = RecipeSource.RECENT.title,
            selected = selected == RecipeSource.RECENT,
            enabled = hasRecent,
            onClick = { onSelected(RecipeSource.RECENT) }
        )
    }
}

@Composable
private fun RecipeSourceChip(
    title: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) Color(0xFFF59B5A) else Color.White
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (selected) Color.White else Color(0xFF666666),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun PortionTotals(
    portionGrams: Int,
    kcal: Int,
    proteins: Float,
    fats: Float,
    carbs: Float
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Итог на порцию ${portionGrams}г",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2A2A2A)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TotalValue(title = "Ккал", value = kcal.pretty())
                TotalValue(title = "Белки", value = proteins.pretty())
                TotalValue(title = "Жиры", value = fats.pretty())
                TotalValue(title = "Углеводы", value = carbs.pretty())
            }
        }
    }
}

@Composable
private fun PortionTotalsCompact(
    portionGrams: Int,
    kcal: Int,
    proteins: Float,
    fats: Float,
    carbs: Float,
    accentColor: Color
) {
    Surface(shape = RoundedCornerShape(12.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Итого на порцию ${portionGrams}г",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2A2A2A)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TotalValueColored(title = "Ккал", value = kcal.pretty(), valueColor = accentColor)
                TotalValueColored(title = "Б", value = proteins.pretty(), valueColor = Color(0xFFB14C0C))
                TotalValueColored(title = "Ж", value = fats.pretty(), valueColor = Color(0xFFFF8A5C))
                TotalValueColored(title = "У", value = carbs.pretty(), valueColor = Color(0xFFE64A19))
            }
        }
    }
}

@Composable
private fun RowScope.TotalValue(title: String, value: String) {
    Column(modifier = Modifier.weight(1f)) {
        Text(text = title, style = MaterialTheme.typography.bodySmall, color = Color(0xFF8A8A8A))
        Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RowScope.TotalValueColored(title: String, value: String, valueColor: Color) {
    Column(modifier = Modifier.weight(1f)) {
        Text(text = title, style = MaterialTheme.typography.bodySmall, color = Color(0xFF8A8A8A))
        Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun PortionQuickActions(
    portionValue: Int,
    onPortionChange: (Int) -> Unit
) {
    val presets = listOf(50, 100, 150, 200)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { preset ->
                PortionChip(
                    text = "${preset}г",
                    selected = portionValue == preset,
                    onClick = { onPortionChange(preset) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            PortionChip(text = "-10", selected = false, onClick = {
                onPortionChange((portionValue - 10).coerceAtLeast(0))
            })
            PortionChip(text = "+10", selected = false, onClick = {
                onPortionChange(portionValue + 10)
            })
        }
    }
}

@Composable
private fun PortionChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) Color(0xFFF59B5A) else Color.White
    val content = if (selected) Color.White else Color(0xFF666666)
    Surface(onClick = onClick, shape = RoundedCornerShape(10.dp), color = background) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = content
        )
    }
}

private fun List<RecipeMealOption>.searchAndSortRecipes(
    query: String,
    source: RecipeSource,
    recentRecipeIds: List<Int>
): List<RecipeMealOption> {
    val base = distinctBy { it.id }
    val normalizedQuery = query.normalizeForRecipeSearch()
    if (normalizedQuery.isBlank()) {
        return when (source) {
            RecipeSource.RECENT -> base
            else -> base.sortedByDescending { it.sortDateKey.ifBlank { it.id.toString().padStart(10, '0') } }
        }
    }
    val tokens = normalizedQuery.split(' ').filter { it.isNotBlank() }
    return base.mapNotNull { recipe ->
        val searchable = recipe.searchableText
        if (tokens.all { token -> searchable.contains(token) }) {
            recipe to recipe.searchScore(normalizedQuery, tokens, recentRecipeIds)
        } else {
            null
        }
    }.sortedWith(
        compareByDescending<Pair<RecipeMealOption, Int>> { it.second }
            .thenByDescending { it.first.sortDateKey.ifBlank { it.first.id.toString().padStart(10, '0') } }
            .thenBy { it.first.title }
    ).map { it.first }
}

private val RecipeMealOption.searchableText: String
    get() = (listOf(title, authorName, calories) + tags).joinToString(" ").normalizeForRecipeSearch()

private val RecipeMealOption.sortDateKey: String
    get() = date.split('.').takeIf { it.size == 3 }?.let { (day, month, year) -> "$year$month$day" }.orEmpty()

private fun RecipeMealOption.searchScore(query: String, tokens: List<String>, recentRecipeIds: List<Int>): Int {
    val titleText = title.normalizeForRecipeSearch()
    val tagText = tags.joinToString(" ").normalizeForRecipeSearch()
    val recentBonus = recentRecipeIds.indexOf(id).takeIf { it >= 0 }?.let { (recentRecipeIds.size - it) * 2 } ?: 0
    return recentBonus + when {
        titleText == query -> 1000
        titleText.startsWith(query) -> 800
        tokens.all { titleText.split(' ').any { word -> word.startsWith(it) } } -> 650
        titleText.contains(query) -> 500
        tokens.any { tagText.contains(it) } -> 300
        else -> 100
    }
}

private fun String.normalizeForRecipeSearch(): String = lowercase()
    .replace('ё', 'е')
    .replace(Regex("[^a-zа-я0-9]+"), " ")
    .trim()

private fun highlightQuery(text: String, query: String, highlightColor: Color): androidx.compose.ui.text.AnnotatedString {
    val trimmedQuery = query.trim()
    if (trimmedQuery.isEmpty()) return buildAnnotatedString { append(text) }
    val lowerText = text.lowercase()
    val lowerQuery = trimmedQuery.lowercase()
    return buildAnnotatedString {
        var index = 0
        while (index < text.length) {
            val matchIndex = lowerText.indexOf(lowerQuery, startIndex = index)
            if (matchIndex < 0) {
                append(text.substring(index))
                break
            }
            append(text.substring(index, matchIndex))
            withStyle(SpanStyle(color = highlightColor, fontWeight = FontWeight.SemiBold)) {
                append(text.substring(matchIndex, matchIndex + lowerQuery.length))
            }
            index = matchIndex + lowerQuery.length
        }
    }
}
