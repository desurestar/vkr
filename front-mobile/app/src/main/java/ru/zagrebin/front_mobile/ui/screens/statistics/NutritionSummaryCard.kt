package ru.zagrebin.front_mobile.ui.screens.statistics

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

@Composable
fun NutritionSummaryCard(day: StatisticsDay) {
    val meals = day.meals.values.flatten()
    val consumedKcal = meals.sumOf { it.kcal }
    val proteins = meals.sumOf { it.proteins.toDouble() }.toInt()
    val fats = meals.sumOf { it.fats.toDouble() }.toInt()
    val carbs = meals.sumOf { it.carbs.toDouble() }.toInt()
    val remainingKcal = (day.goalKcal - consumedKcal).coerceAtLeast(0)

    CaloriesCard(
        remainingKcal = remainingKcal,
        totalKcal = day.goalKcal,
        consumedKcal = consumedKcal,
        protein = MacroValue(proteins, day.proteinGoalGrams),
        fat = MacroValue(fats, day.fatGoalGrams),
        carbs = MacroValue(carbs, day.carbsGoalGrams),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CaloriesCard(
    remainingKcal: Int,
    totalKcal: Int,
    consumedKcal: Int,
    protein: MacroValue,
    fat: MacroValue,
    carbs: MacroValue,
    modifier: Modifier = Modifier
) {
    val progress = if (totalKcal <= 0) 0f
    else (1f - min(1f, remainingKcal.toFloat() / totalKcal.toFloat()))

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressArcWithText(
                progress = progress,
                size = 150.dp,
                stroke = 12.dp,
                trackColor = Color(0xFFE2E2E2),
                progressColor = Color(0xFFD45A1A),
                remainingKcal = remainingKcal,
                totalKcal = totalKcal,
                consumedKcal = consumedKcal
            )

            Spacer(Modifier.width(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                MacroRow(color = Color(0xFFB14C0C), title = "Белки", value = protein)
                MacroRow(color = Color(0xFFFF8A5C), title = "Жиры", value = fat)
                MacroRow(color = Color(0xFFE64A19), title = "Углеводы", value = carbs)
            }
        }
    }
}

@Composable
private fun ProgressArcWithText(
    progress: Float,
    size: Dp,
    stroke: Dp,
    trackColor: Color,
    progressColor: Color,
    remainingKcal: Int,
    totalKcal: Int,
    consumedKcal: Int
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        ProgressArc(
            progress = progress,
            size = size,
            stroke = stroke,
            trackColor = trackColor,
            progressColor = progressColor
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$remainingKcal ккал",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2A2A2A)
            )
            Text(
                text = "осталось из $totalKcal",
                fontSize = 12.sp,
                color = Color(0xFF8A8A8A)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Всего: $consumedKcal",
                fontSize = 12.sp,
                color = Color(0xFFB3927D),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProgressArc(
    progress: Float,
    size: Dp,
    stroke: Dp,
    trackColor: Color,
    progressColor: Color
) {
    val strokePx = with(LocalDensity.current) { stroke.toPx() }
    Canvas(
        modifier = Modifier.size(size)
    ) {
        val startAngle = 180f
        val sweepAngle = 180f
        val inset = strokePx / 2f
        val arcSize = Size(size.toPx() - strokePx, size.toPx() - strokePx)
        val topLeft = Offset(inset, inset)

        drawArc(
            color = trackColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
        drawArc(
            color = progressColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle * progress.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun MacroRow(color: Color, title: String, value: MacroValue) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = value.current.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = "/${value.total}г",
            fontSize = 12.sp,
            color = Color(0xFF8A8A8A)
        )
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(color, shape = RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color(0xFF8A8A8A)
        )
    }
}

data class MacroValue(val current: Int, val total: Int)

@Preview(showBackground = true, locale = "ru")
@Composable
private fun NutritionSummaryCardPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            CaloriesCard(
                remainingKcal = 158,
                totalKcal = 1000,
                consumedKcal = 842,
                protein = MacroValue(28, 50),
                fat = MacroValue(41, 33),
                carbs = MacroValue(85, 125)
            )
        }
    }
}
