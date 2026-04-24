package ru.zagrebin.front_mobile.ui.components.recipeTag

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TagScreen(
    state: TagState,
    onClick: () -> Unit
) {
    val bgColor = if (state.isHighlighted) Color(0xFFB57A1D) else Color(0xFFE9E6E0)
    val textColor = if (state.isHighlighted) Color.White else Color(0xFF444444)

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .padding(end = 6.dp, bottom = 6.dp)
            .clickable { onClick() }
    ) {
        Text(
            text = state.title,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}