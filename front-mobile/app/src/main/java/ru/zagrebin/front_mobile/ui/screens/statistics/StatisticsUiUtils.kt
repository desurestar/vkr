package ru.zagrebin.front_mobile.ui.screens.statistics

import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

@Composable
fun inputColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent
)

fun sanitizeDecimal(input: String): String {
    val cleaned = input.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
    val firstDot = cleaned.indexOf('.')
    return if (firstDot >= 0) {
        cleaned.substring(0, firstDot + 1) + cleaned.substring(firstDot + 1).replace(".", "")
    } else {
        cleaned
    }
}

fun Number.pretty(): String {
    return when (this) {
        is Int -> toString()
        is Float -> {
            val rounded = (this * 10f).roundToInt() / 10f
            if (rounded % 1f == 0f) rounded.toInt().toString() else rounded.toString()
        }

        is Double -> {
            val rounded = (this * 10.0).roundToInt() / 10.0
            if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
        }

        else -> toString()
    }
}

