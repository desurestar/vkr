package ru.zagrebin.front_mobile.ui.screens.statistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayStrip(
    days: List<StatisticsDay>,
    selectedDayId: Int,
    monthLabel: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    var showCalendar by remember { mutableStateOf(false) }

    LaunchedEffect(days.firstOrNull()?.id, days.size, selectedDayId) {
        val selectedIndex = days.indexOfFirst { it.id == selectedDayId }
        if (selectedIndex >= 0) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MonthNavButton(text = "‹", onClick = onPreviousMonth)
            Surface(
                onClick = { showCalendar = true },
                shape = RoundedCornerShape(10.dp),
                color = Color.White
            ) {
                Text(
                    text = monthLabel.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2A2A2A)
                )
            }
            MonthNavButton(text = "›", onClick = onNextMonth)
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            state = listState,
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

    if (showCalendar) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDayId.toUtcMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showCalendar = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis
                            ?.toDayId()
                            ?.let(onDayClick)
                        showCalendar = false
                    }
                ) {
                    Text(text = "Выбрать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCalendar = false }) {
                    Text(text = "Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun MonthNavButton(text: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(10.dp), color = Color.White) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFF59B5A),
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun DayStripPreview() {
    DayStrip(
        days = previewStatisticsUiState().days,
        selectedDayId = 30,
        monthLabel = "июнь 2026",
        onPreviousMonth = {},
        onNextMonth = {},
        onDayClick = {}
    )
}

private fun Int.toUtcMillis(): Long = LocalDate
    .ofEpochDay(toLong())
    .atStartOfDay(ZoneOffset.UTC)
    .toInstant()
    .toEpochMilli()

private fun Long.toDayId(): Int = Instant
    .ofEpochMilli(this)
    .atZone(ZoneOffset.UTC)
    .toLocalDate()
    .toEpochDay()
    .toInt()
