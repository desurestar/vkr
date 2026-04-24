package ru.zagrebin.front_mobile.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun NutritionSummaryCard(day: StatisticsDay) {
    val meals = day.meals.values.flatten()
    val consumedKcal = meals.sumOf { it.kcal }
    val proteins = meals.sumOf { it.proteins.toDouble() }.toFloat()
    val fats = meals.sumOf { it.fats.toDouble() }.toFloat()
    val carbs = meals.sumOf { it.carbs.toDouble() }.toFloat()
    val remainingKcal = (day.goalKcal - consumedKcal).coerceAtLeast(0)

    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ArcCalorieProgress(
                    consumed = consumedKcal,
                    goal = day.goalKcal,
                    remaining = remainingKcal,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp, top = 2.dp, bottom = 2.dp)
                )

                Column(
                    modifier = Modifier.weight(1f).padding(start = 8.dp, end = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MacroStat(value = proteins, label = "Белки", icon = "🥚")
                    MacroStat(value = fats, label = "Жиры", icon = "🧈")
                    MacroStat(value = carbs, label = "Углеводы", icon = "🌾")
                }
            }
        }
    }
}

@Composable
private fun ArcCalorieProgress(
    consumed: Int,
    goal: Int,
    remaining: Int,
    modifier: Modifier = Modifier
) {
    val progress = (consumed.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    Box(modifier = modifier.padding(horizontal = 10.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .size(140.dp)
                .padding(8.dp)
        ) {
            val stroke = 10.dp.toPx()

            val diameter = size.minDimension * 1.4f
            val arcSize = Size(diameter, diameter)

            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )

            drawArc(
                color = Color(0xFFDCDCDC),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )

            drawArc(
                color = Color(0xFFE85318),
                startAngle = 180f,
                sweepAngle = 180f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.height(66.dp)
        ) {
            Text(
                text = "$consumed ккал",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "осталось из $goal",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = "Всего: $remaining",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFB17C58)
            )
        }
    }
}

@Composable
private fun MacroStat(value: Float, label: String, icon: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),

    ) {
        Text(
            text = value.pretty(),
            color = Color(0xFFE85318),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "•",
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = label,
            color = Color(0xFF555555),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun NutritionSummaryCardPreview() {
    NutritionSummaryCard(day = previewStatisticsDay())
}

