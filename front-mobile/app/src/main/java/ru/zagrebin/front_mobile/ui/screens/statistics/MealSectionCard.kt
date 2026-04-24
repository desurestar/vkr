package ru.zagrebin.front_mobile.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MealSectionCard(
    type: MealType,
    entries: List<MealEntry>,
    onAddClick: () -> Unit
) {
    val kcal = entries.sumOf { it.kcal }
    val proteins = entries.sumOf { it.proteins.toDouble() }.toFloat()
    val fats = entries.sumOf { it.fats.toDouble() }.toFloat()
    val carbs = entries.sumOf { it.carbs.toDouble() }.toFloat()

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(10.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 42.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = type.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)

                if (entries.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MealInfoChip(value = kcal.pretty(), label = "ккал:")
                        MealInfoChip(value = proteins.pretty(), label = "б:")
                        MealInfoChip(value = fats.pretty(), label = "ж:")
                        MealInfoChip(value = carbs.pretty(), label = "у:")
                    }
                } else {
                    Spacer(modifier = Modifier.size(28.dp))
                }
            }

            IconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(30.dp)
                    .background(Color(0xFFEAEAEA), CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить", tint = Color(0xFF666666))
            }
        }
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun MealSectionCardPreview() {
    MealSectionCard(
        type = MealType.BREAKFAST,
        entries = previewMealEntries(),
        onAddClick = {}
    )
}

