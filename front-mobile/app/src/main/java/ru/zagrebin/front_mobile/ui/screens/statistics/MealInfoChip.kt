package ru.zagrebin.front_mobile.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MealInfoChip(value: String, label: String) {
    Row(
        modifier = Modifier
            .background(Color(0xFFFAF5F0), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(text = label, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
        Text(text = value, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun MealInfoChipPreview() {
    MealInfoChip(value = "370.6", label = "ккал")
}

