package ru.zagrebin.front_mobile.ui.screens.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text


import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext


import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle


import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.navigation.compose.rememberNavController
import compose.icons.FeatherIcons
import compose.icons.feathericons.Lock
import compose.icons.feathericons.Mail
import compose.icons.feathericons.User


import ru.zagrebin.front_mobile.R
import ru.zagrebin.front_mobile.data.AppContainer
import ru.zagrebin.front_mobile.data.remote.api.AuthRequest
import ru.zagrebin.front_mobile.ui.components.AnimatedLabelTextField
import ru.zagrebin.front_mobile.ui.navigation.Screen
import ru.zagrebin.front_mobile.ui.navigation.AuthSessionState
import ru.zagrebin.front_mobile.ui.navigation.BottomNavItem

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = AppContainer(context).feedApi

    var login by remember { mutableStateOf("") }
    var email by remember {mutableStateOf("")}
    var password by remember {mutableStateOf("")}

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.registration),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
        )

        Box(modifier = Modifier
            .width(390.dp)
            .height(690.dp)
            .padding(horizontal = 20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    // контент
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {

                        AnimatedLabelTextField(
                            value = login,
                            onValueChange = {login = it},
                            labelText = "Логин",
                            icon = FeatherIcons.User
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        AnimatedLabelTextField(
                            value = email,
                            onValueChange = {email = it},
                            labelText = "Email",
                            icon = FeatherIcons.Mail
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        AnimatedLabelTextField(
                            value = password,
                            onValueChange = {password = it},
                            labelText = "Пароль",
                            icon = FeatherIcons.Lock,
                            isPassword = true
                        )

                        Spacer(modifier = Modifier.height(60.dp))

                        Button(
                            onClick = {
                                if (login.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                                    scope.launch {
                                        runCatching {
                                            api.register(AuthRequest(email = email.trim(), password = password, username = login.trim()))
                                        }.onSuccess {
                                            AuthSessionState.setAuthorized(context, true)
                                            navController.navigate(BottomNavItem.Profile.route) {
                                                popUpTo(Screen.EntryOptions.route) { inclusive = true }
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFFFFF),      // фон кнопки
                                contentColor = Color(0xFF1E1C1F),              // цвет текста/иконок
                                disabledContainerColor = Color.Gray,     // фон, когда disabled
                                disabledContentColor = Color.LightGray   // текст, когда disabled
                            ),

                        ) {
                            Text("Создать аккаутн", fontSize = 17.sp, color = Color(0xFF1E1C1F))
                        }

                        Spacer(modifier = Modifier.height(60.dp))

                        Text(
                            text = buildAnnotatedString {
                                append("Уже есть аккаунт? ")

                                withStyle(
                                    style = SpanStyle(
                                        textDecoration = TextDecoration.Underline,
                                        color = Color.White
                                    )
                                ) {
                                    append("Войти")
                                }
                            },
                            fontSize = 14.sp,
                            color = Color.White,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.Login.route)
                            }
                         )
                    }
                }
            }

        }
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
fun RegisterScreenPreview() {
    val navController = rememberNavController()
    RegisterScreen(navController)
}
