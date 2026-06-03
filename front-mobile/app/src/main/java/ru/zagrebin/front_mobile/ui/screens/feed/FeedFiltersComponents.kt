package ru.zagrebin.front_mobile.ui.screens.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagScreen
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagState
import ru.zagrebin.front_mobile.ui.theme.AppPageBackgroundColor
import ru.zagrebin.front_mobile.ui.theme.FilterFieldContainerColor
import ru.zagrebin.front_mobile.ui.theme.FilterFieldCornerRadius
import ru.zagrebin.front_mobile.ui.theme.FilterRangeFieldsSpacing
import ru.zagrebin.front_mobile.ui.theme.FilterSheetBottomSpacer
import ru.zagrebin.front_mobile.ui.theme.FilterSheetContentSpacing
import ru.zagrebin.front_mobile.ui.theme.FilterSheetCornerRadius
import ru.zagrebin.front_mobile.ui.theme.FilterSheetHorizontalPadding
import ru.zagrebin.front_mobile.ui.theme.FilterSheetVerticalPadding
import ru.zagrebin.front_mobile.ui.theme.SearchFieldContainerColor
import ru.zagrebin.front_mobile.ui.theme.TransparentColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeedFiltersSheet(
    title: String,
    filters: FeedFilters,
    tagQuery: String,
    tagSuggestions: List<TagState>,
    showRecipeRanges: Boolean,
    onFilterChange: (FeedFilters) -> Unit,
    onTagQueryChange: (String) -> Unit,
    onAddTag: (TagState) -> Unit,
    onRemoveTag: (Int) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppPageBackgroundColor,
        contentColor = MaterialTheme.colorScheme.onBackground,
        shape = RoundedCornerShape(
            topStart = FilterSheetCornerRadius,
            topEnd = FilterSheetCornerRadius
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = FilterSheetHorizontalPadding,
                    vertical = FilterSheetVerticalPadding
                )
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(FilterSheetContentSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "Подберите публикации по нужным параметрам",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Закрыть")
                }
            }

            if (showRecipeRanges) {
                FilterSectionTitle("Время готовки, мин")
                FilterRangeRow(
                    fromValue = filters.minTime,
                    toValue = filters.maxTime,
                    onFromChange = { onFilterChange(filters.copy(minTime = it.onlyDecimalInput())) },
                    onToChange = { onFilterChange(filters.copy(maxTime = it.onlyDecimalInput())) }
                )

                FilterSectionTitle("Калории, ккал")
                FilterRangeRow(
                    fromValue = filters.minCalories,
                    toValue = filters.maxCalories,
                    onFromChange = { onFilterChange(filters.copy(minCalories = it.onlyDecimalInput())) },
                    onToChange = { onFilterChange(filters.copy(maxCalories = it.onlyDecimalInput())) }
                )

                FilterSectionTitle("Белки на 100 г")
                FilterRangeRow(
                    fromValue = filters.minProteins,
                    toValue = filters.maxProteins,
                    onFromChange = { onFilterChange(filters.copy(minProteins = it.onlyDecimalInput())) },
                    onToChange = { onFilterChange(filters.copy(maxProteins = it.onlyDecimalInput())) }
                )

                FilterSectionTitle("Жиры на 100 г")
                FilterRangeRow(
                    fromValue = filters.minFats,
                    toValue = filters.maxFats,
                    onFromChange = { onFilterChange(filters.copy(minFats = it.onlyDecimalInput())) },
                    onToChange = { onFilterChange(filters.copy(maxFats = it.onlyDecimalInput())) }
                )

                FilterSectionTitle("Углеводы на 100 г")
                FilterRangeRow(
                    fromValue = filters.minCarbs,
                    toValue = filters.maxCarbs,
                    onFromChange = { onFilterChange(filters.copy(minCarbs = it.onlyDecimalInput())) },
                    onToChange = { onFilterChange(filters.copy(maxCarbs = it.onlyDecimalInput())) }
                )
            }

            FilterSectionTitle("Теги")
            TextField(
                value = tagQuery,
                onValueChange = onTagQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Найти тег") },
                singleLine = true,
                shape = RoundedCornerShape(FilterFieldCornerRadius),
                colors = filterTextFieldColors()
            )

            if (filters.selectedTags.isNotEmpty()) {
                Text(
                    text = "Выбрано",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow {
                    filters.selectedTags.forEach { tag ->
                        TagScreen(
                            state = tag.copy(isHighlighted = true),
                            onClick = { onRemoveTag(tag.id) }
                        )
                    }
                }
            }

            if (tagSuggestions.isNotEmpty()) {
                Text(
                    text = "Подходящие теги",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow {
                    tagSuggestions.forEach { tag ->
                        TagScreen(
                            state = tag,
                            onClick = { onAddTag(tag) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Сбросить")
                }
                Button(
                    onClick = onApply,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Применить")
                }
            }

            Spacer(modifier = Modifier.height(FilterSheetBottomSpacer))
        }
    }
}

@Composable
private fun FilterSectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun FilterRangeRow(
    fromValue: String,
    toValue: String,
    onFromChange: (String) -> Unit,
    onToChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FilterRangeFieldsSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = fromValue,
            onValueChange = onFromChange,
            placeholder = { Text("От") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(FilterFieldCornerRadius),
            colors = filterTextFieldColors()
        )

        Text(text = "—")

        TextField(
            value = toValue,
            onValueChange = onToChange,
            placeholder = { Text("До") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(FilterFieldCornerRadius),
            colors = filterTextFieldColors()
        )
    }
}

@Composable
private fun filterTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = FilterFieldContainerColor,
    unfocusedContainerColor = SearchFieldContainerColor,
    focusedIndicatorColor = TransparentColor,
    unfocusedIndicatorColor = TransparentColor,
    disabledIndicatorColor = TransparentColor
)

private fun String.onlyDecimalInput(): String = filterIndexed { index, char ->
    char.isDigit() || (char == '.' && index > 0 && take(index).none { it == '.' })
}
