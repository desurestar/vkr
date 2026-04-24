package ru.zagrebin.front_mobile.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun WaterCard(
    consumedMl: Int,
    goalMl: Int,
    onAdd: (Int) -> Unit
) {
    var showWaterDialog by rememberSaveable { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth(),
        onClick = { showWaterDialog = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalDrink,
                contentDescription = null,
                tint = Color(0xFF3FA8FF)
            )
            Column(modifier = Modifier.weight(1f)) {
                val liters = consumedMl / 1000f
                val goalLiters = goalMl / 1000f
                Text(
                    text = "${liters.pretty()} / ${goalLiters.pretty()} л",
                    color = Color(0xFF1D77CC),
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Вода", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Surface(
                shape = RoundedCornerShape(99.dp),
                color = Color(0xFFD7E8FF),
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF4A90E2), modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    if (showWaterDialog) {
        WaterAddBottomSheet(
            currentMl = consumedMl,
            onDismiss = { showWaterDialog = false },
            onAdd = { amountMl ->
                onAdd(amountMl)
                showWaterDialog = false
            }
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun WaterAddBottomSheet(
    currentMl: Int,
    onDismiss: () -> Unit,
    onAdd: (Int) -> Unit
) {
    var amountMl by rememberSaveable { mutableStateOf(250) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFF2F2F2)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Добавление воды",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF555555)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEFF5FF), RoundedCornerShape(14.dp))
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalDrink,
                        contentDescription = null,
                        tint = Color(0xFF1D5FFF),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "💧",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color(0xFF1D5FFF)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    onClick = { amountMl = (amountMl - 50).coerceAtLeast(50) },
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "−", style = MaterialTheme.typography.headlineSmall, color = Color(0xFFB665FF))
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = (amountMl / 1000f).pretty(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF555555)
                    )
                    Text(text = "литра", color = Color(0xFF9A9A9A), style = MaterialTheme.typography.bodySmall)
                }

                Surface(
                    onClick = { amountMl = (amountMl + 50).coerceAtMost(3000) },
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF7C3AED),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Text(
                text = "Сейчас: ${(currentMl / 1000f).pretty()} л",
                color = Color(0xFF8A8A8A),
                style = MaterialTheme.typography.bodySmall
            )

            Surface(
                onClick = { onAdd(amountMl) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(listOf(Color(0xFFB000FF), Color(0xFF2A44FF))),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Добавить", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun WaterCardPreview() {
    WaterCard(consumedMl = 500, goalMl = 1500, onAdd = {})
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun WaterAddBottomSheetPreview() {
    WaterAddBottomSheet(currentMl = 500, onDismiss = {}, onAdd = {})
}


