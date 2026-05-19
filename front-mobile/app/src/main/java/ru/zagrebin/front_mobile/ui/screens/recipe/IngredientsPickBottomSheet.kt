package ru.zagrebin.front_mobile.ui.screens.recipe

import android.annotation.SuppressLint
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsPickBottomSheet(
    ingredients: List<IngredientPickUi>,
    selected: SnapshotStateMap<Int, Boolean>,
    onDismiss: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedCount = ingredients.count { selected[it.id] == true }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF7F5F2),
        scrimColor = Color.Black.copy(alpha = 0.32f)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
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
                Text(
                    text = "Выберите ингредиенты",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$selectedCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF8E8E8E)
                )
            }

            HorizontalDivider(color = Color(0xFFE1E1E1))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item { Spacer(modifier = Modifier.height(6.dp)) }
                items(ingredients, key = { it.id }) { ingredient ->
                    IngredientPickRow(
                        ingredient = ingredient,
                        isChecked = selected[ingredient.id] == true,
                        onCheckedChange = { checked ->
                            selected[ingredient.id] = checked
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(6.dp)) }
            }

            HorizontalDivider(color = Color(0xFFE1E1E1))

            Button(
                onClick = onAddClick,
                enabled = selectedCount > 0,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C3AED),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(text = "Добавить")
            }
        }
    }
}

@Composable
private fun IngredientPickRow(
    ingredient: IngredientPickUi,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF7C3AED))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = ingredient.title, style = MaterialTheme.typography.bodyMedium)
    }
}

data class IngredientPickUi(
    val id: Int,
    val title: String
)

@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true, locale = "ru")
@Composable
private fun IngredientsPickBottomSheetPreview() {
    val selected = androidx.compose.runtime.mutableStateMapOf(
        0 to true,
        1 to false,
        2 to true
    )
    IngredientsPickBottomSheet(
        ingredients = listOf(
            IngredientPickUi(0, "Творог - 500 г"),
            IngredientPickUi(1, "Яйцо - 1 шт"),
            IngredientPickUi(2, "Сахар - 2 ст. л.")
        ),
        selected = selected,
        onDismiss = {},
        onAddClick = {}
    )
}

