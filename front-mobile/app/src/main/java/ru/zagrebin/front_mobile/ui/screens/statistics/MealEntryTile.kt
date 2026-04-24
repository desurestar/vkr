package ru.zagrebin.front_mobile.ui.screens.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MealEntryTile(meal: MealEntry) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.height(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = meal.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )

                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    MiniMacro("Б", meal.proteins)
                    MiniMacro("Ж", meal.fats)
                    MiniMacro("У", meal.carbs)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = meal.timeLabel, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Box() {
                    Text(
                        text = meal.amountLabel,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(text = "${meal.kcal.pretty()} ккал", fontWeight = FontWeight.Bold, color = Color(0xFF3A3A3A))
            }
        }
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun MealEntryTilePreview() {
    MealEntryTile(meal = previewMealEntries().first())
}

