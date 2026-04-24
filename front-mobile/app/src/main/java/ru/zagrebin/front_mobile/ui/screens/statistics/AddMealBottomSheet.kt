package ru.zagrebin.front_mobile.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AddMealBottomSheet(
    mealType: MealType,
    onDismiss: () -> Unit,
    onAddClick: () -> Unit
) {
    var title by rememberSaveable(mealType) { mutableStateOf("") }
    var portion by rememberSaveable(mealType) { mutableStateOf("0") }
    var isLiquid by rememberSaveable(mealType) { mutableStateOf(false) }
    var proteins100 by rememberSaveable(mealType) { mutableStateOf("0") }
    var fats100 by rememberSaveable(mealType) { mutableStateOf("0") }
    var carbs100 by rememberSaveable(mealType) { mutableStateOf("0") }
    var kcal100 by rememberSaveable(mealType) { mutableStateOf("0") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFEFEFEF)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Название",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFA4A4A4),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = Color(0xFFA4A4A4)
                )
            }

            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Введите название") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = inputColors()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Порция гр", color = Color(0xFF666666), style = MaterialTheme.typography.bodyMedium)
                    TextField(
                        value = portion,
                        onValueChange = { portion = it.filter { ch -> ch.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = inputColors()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Жидкость", color = Color(0xFF666666), style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .background(Color.White, RoundedCornerShape(99.dp))
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = isLiquid,
                            onCheckedChange = { isLiquid = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFF59B5A),
                                uncheckedThumbColor = Color(0xFFF59B5A),
                                checkedTrackColor = Color.White,
                                uncheckedTrackColor = Color.White,
                                checkedBorderColor = Color.Transparent,
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            Text(
                text = "Укажите КБЖУ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KbjuField(
                    title = "Белки на 100 гр",
                    value = proteins100,
                    onValueChange = { proteins100 = sanitizeDecimal(it) },
                    modifier = Modifier.weight(1f)
                )
                KbjuField(
                    title = "Жиры на 100 гр",
                    value = fats100,
                    onValueChange = { fats100 = sanitizeDecimal(it) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KbjuField(
                    title = "Углеводы на 100 гр",
                    value = carbs100,
                    onValueChange = { carbs100 = sanitizeDecimal(it) },
                    modifier = Modifier.weight(1f)
                )
                KbjuField(
                    title = "ККал на 100 гр",
                    value = kcal100,
                    onValueChange = { kcal100 = it.filter { ch -> ch.isDigit() } },
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            Surface(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF7C3AED),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "Добавить в ${mealType.title}",
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun KbjuField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Decimal
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.bodySmall, color = Color(0xFF666666))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = inputColors()
        )
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun KbjuFieldPreview() {
    KbjuField(title = "Белки на 100 гр", value = "10.5", onValueChange = {})
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun AddMealBottomSheetTriggerPreview() {
    Surface(color = Color(0xFFEFEFEF), modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "BottomSheet открывается по клику '+'",
            modifier = Modifier.padding(16.dp),
            color = Color(0xFF666666)
        )
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun AddMealBottomSheetPreview() {
    AddMealBottomSheet(
        mealType = MealType.BREAKFAST,
        onDismiss = {},
        onAddClick = {}
    )
}


