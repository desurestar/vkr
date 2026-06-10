package ru.zagrebin.front_mobile.ui.screens.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import ru.zagrebin.front_mobile.ui.theme.LightPrimary

@Composable
fun StatisticsSettingsCard(
    settings: StatisticsSettings,
    onSave: (StatisticsSettings) -> Unit
) {
    var showSheet by rememberSaveable { mutableStateOf(false) }
    Surface(
        onClick = { showSheet = true },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Настройки статистики", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "История: ${settings.retentionMonths} мес. · Цель: ${settings.goalKcal} ккал, Б${settings.proteinGoalGrams}/Ж${settings.fatGoalGrams}/У${settings.carbsGoalGrams} г",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF777777)
            )
        }
    }

    if (showSheet) {
        StatisticsSettingsBottomSheet(
            settings = settings,
            onDismiss = { showSheet = false },
            onSave = {
                onSave(it)
                showSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatisticsSettingsBottomSheet(
    settings: StatisticsSettings,
    onDismiss: () -> Unit,
    onSave: (StatisticsSettings) -> Unit
) {
    var retention by rememberSaveable(settings) {
        mutableStateOf(settings.retentionMonths.toString())
    }
    var kcal by rememberSaveable(settings) {
        mutableStateOf(settings.goalKcal.toString())
    }
    var water by rememberSaveable(settings) {
        mutableStateOf(settings.waterGoalMl.toString())
    }
    var proteins by rememberSaveable(settings) {
        mutableStateOf(settings.proteinGoalGrams.toString())
    }
    var fats by rememberSaveable(settings) {
        mutableStateOf(settings.fatGoalGrams.toString())
    }
    var carbs by rememberSaveable(settings) {
        mutableStateOf(settings.carbsGoalGrams.toString())
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF2F2F2)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "Настройка целей",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            SettingsNumberField(
                label = "Хранить историю, месяцев",
                value = retention
            ) { retention = it }

            SettingsNumberField(
                label = "Калорий в день",
                value = kcal
            ) { kcal = it }

            SettingsNumberField(
                label = "Вода, мл в день",
                value = water
            ) { water = it }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsNumberField(
                    label = "Белки, г",
                    value = proteins,
                    modifier = Modifier.weight(1f)
                ) {
                    proteins = it
                }

                SettingsNumberField(
                    label = "Жиры, г",
                    value = fats,
                    modifier = Modifier.weight(1f)
                ) {
                    fats = it
                }

                SettingsNumberField(
                    label = "Углеводы, г",
                    value = carbs,
                    modifier = Modifier.weight(1f)
                ) {
                    carbs = it
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        onSave(
                            StatisticsSettings(
                                retentionMonths = retention.toIntOrNull()
                                    ?.coerceIn(1, 24) ?: 3,
                                goalKcal = kcal.toIntOrNull()
                                    ?.coerceAtLeast(1) ?: settings.goalKcal,
                                waterGoalMl = water.toIntOrNull()
                                    ?.coerceAtLeast(0) ?: settings.waterGoalMl,
                                proteinGoalGrams = proteins.toIntOrNull()
                                    ?.coerceAtLeast(0) ?: settings.proteinGoalGrams,
                                fatGoalGrams = fats.toIntOrNull()
                                    ?.coerceAtLeast(0) ?: settings.fatGoalGrams,
                                carbsGoalGrams = carbs.toIntOrNull()
                                    ?.coerceAtLeast(0) ?: settings.carbsGoalGrams
                            )
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightPrimary
                    )
                ) {
                    Text(
                        text = "Сохранить",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsNumberField(label: String, value: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter(Char::isDigit)) },
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}
