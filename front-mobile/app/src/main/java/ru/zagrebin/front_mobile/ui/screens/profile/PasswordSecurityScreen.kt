package ru.zagrebin.front_mobile.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.zagrebin.front_mobile.ui.theme.AppPageBackgroundColor
import ru.zagrebin.front_mobile.ui.theme.LightPrimary
import ru.zagrebin.front_mobile.ui.theme.LightSecondary
import ru.zagrebin.front_mobile.ui.theme.ListBottomPadding

@Composable
fun PasswordSecurityScreen(
    onBackClick: () -> Unit
) {
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var emailCode by rememberSaveable { mutableStateOf("") }
    var emailNewPassword by rememberSaveable { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    var showCurrentPassword by rememberSaveable { mutableStateOf(false) }
    var showNewPassword by rememberSaveable { mutableStateOf(false) }
    var showEmailNewPassword by rememberSaveable { mutableStateOf(false) }

    val isCurrentValid = currentPassword.length >= 6
    val isNewValid = newPassword.length >= 6
    val isEmailCodeValid = emailCode.length == 6
    val isEmailNewValid = emailNewPassword.length >= 6

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppPageBackgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PasswordSecurityTopBar(onBackClick = onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionCard {
                    SectionTitle(text = "Смена пароля")

                    LabeledPasswordField(
                        label = "Старый пароль",
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        isError = showErrors && !isCurrentValid,
                        placeholder = "Введите текущий пароль",
                        supportingText = if (showErrors && !isCurrentValid) "Минимум 6 символов" else "",
                        isVisible = showCurrentPassword,
                        onVisibilityToggle = { showCurrentPassword = !showCurrentPassword }
                    )

                    LabeledPasswordField(
                        label = "Новый пароль",
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        isError = showErrors && !isNewValid,
                        placeholder = "Введите новый пароль",
                        supportingText = if (showErrors && !isNewValid) "Минимум 6 символов" else "",
                        isVisible = showNewPassword,
                        onVisibilityToggle = { showNewPassword = !showNewPassword }
                    )

                    Button(
                        onClick = { showErrors = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightPrimary),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = "Сменить пароль", color = Color.White)
                    }
                }

                SectionCard {
                    SectionTitle(text = "Смена по почте")

                    Text(
                        text = "Мы отправим код на вашу почту",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8A8A8A)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Outlined.Email, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Отправить код")
                        }
                    }

                    LabeledTextField(
                        label = "Код из письма",
                        value = emailCode,
                        onValueChange = { input ->
                            emailCode = input.filter { it.isDigit() }.take(6)
                        },
                        isError = showErrors && !isEmailCodeValid,
                        placeholder = "6 цифр",
                        supportingText = if (showErrors && !isEmailCodeValid) "Введите 6-значный код" else "",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    LabeledPasswordField(
                        label = "Новый пароль",
                        value = emailNewPassword,
                        onValueChange = { emailNewPassword = it },
                        isError = showErrors && !isEmailNewValid,
                        placeholder = "Введите новый пароль",
                        supportingText = if (showErrors && !isEmailNewValid) "Минимум 6 символов" else "",
                        isVisible = showEmailNewPassword,
                        onVisibilityToggle = { showEmailNewPassword = !showEmailNewPassword }
                    )

                    Button(
                        onClick = { showErrors = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightPrimary),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = "Подтвердить", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(ListBottomPadding))
            }
        }
    }
}

@Composable
private fun PasswordSecurityTopBar(onBackClick: () -> Unit) {
    Surface(color = AppPageBackgroundColor) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад"
                )
            }
            Text(
                text = "Пароль и безопасность",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    placeholder: String,
    supportingText: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF666666))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF2F2F2).copy(alpha = 0.6f),
                unfocusedContainerColor = Color(0xFFF2F2F2).copy(alpha = 0.6f),
                focusedBorderColor = Color(0xFFBDBDBD),
                unfocusedBorderColor = Color(0xFFBDBDBD),
                cursorColor = Color.DarkGray,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            supportingText = {
                if (supportingText.isNotEmpty()) {
                    Text(text = supportingText, color = Color(0xFFD32F2F))
                }
            }
        )
    }
}

@Composable
private fun LabeledPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    placeholder: String,
    supportingText: String,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF666666))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = onVisibilityToggle) {
                    Icon(
                        imageVector = if (isVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF2F2F2).copy(alpha = 0.6f),
                unfocusedContainerColor = Color(0xFFF2F2F2).copy(alpha = 0.6f),
                focusedBorderColor = Color(0xFFBDBDBD),
                unfocusedBorderColor = Color(0xFFBDBDBD),
                cursorColor = Color.DarkGray,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            supportingText = {
                if (supportingText.isNotEmpty()) {
                    Text(text = supportingText, color = Color(0xFFD32F2F))
                }
            }
        )
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun PasswordSecurityScreenPreview() {
    PasswordSecurityScreen(onBackClick = {})
}
