package ru.zagrebin.front_mobile.ui.screens.statistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DayStrip(
    days: List<StatisticsDay>,
    selectedDayId: Int,
    onDayClick: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(days) { day ->
            val selected = day.id == selectedDayId
            Surface(
                onClick = { onDayClick(day.id) },
                shape = CircleShape,
                color = if (selected) Color.White else Color(0xFFB9B9B9),
                border = if (selected) BorderStroke(1.dp, Color(0xFFFF6B6B)) else null
            ) {
                Text(
                    text = day.dayNumber,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) Color(0xFFFF4D4D) else Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun DayStripPreview() {
    DayStrip(
        days = previewStatisticsUiState().days,
        selectedDayId = 30,
        onDayClick = {}
    )
}


