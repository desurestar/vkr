package ru.zagrebin.front_mobile.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import compose.icons.FeatherIcons
import compose.icons.feathericons.Lock
import compose.icons.feathericons.User
import ru.zagrebin.front_mobile.R
import ru.zagrebin.front_mobile.ui.components.AnimatedLabelTextField
import ru.zagrebin.front_mobile.ui.navigation.Screen

@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel = viewModel()) {

    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_login),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier
            .width(390.dp)
            .height(659.dp)
            .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {}
                Box(modifier = Modifier
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
                            value = state.loginOrEmail,
                            onValueChange = viewModel::onLoginOrEmailChange,
                            labelText = "Логин/Email",
                            icon = FeatherIcons.User
                        )

                        Spacer(modifier = Modifier.height((14.dp)))

                        AnimatedLabelTextField(
                            value = state.password,
                            onValueChange = viewModel::onPasswordChange,
                            labelText = "Пароль",
                            icon = FeatherIcons.Lock,
                            isPassword = true
                        )

                        Spacer(modifier = Modifier.height(70.dp))

                        state.error?.let {
                            Text(text = it, color = Color.Red)
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Button(
                            onClick = {viewModel.login()},
                            enabled = !state.isLoading,
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
                            Text(
                                if (state.isLoading) "Загрузка" else "Войти",
                                fontSize = 17.sp,
                                color = Color(0xFF1E1C1F)
                            )

                            if (state.isSuccess) {
                                LaunchedEffect(Unit) {
                                    navController.navigate(Screen.EntryOptions.route)
                                }
                            }



                        }

                        Spacer(modifier = Modifier.height(70.dp))

                        Text(
                            text = buildAnnotatedString {
                                append("Ещё нет аккаунта? ")

                                withStyle(
                                    style = SpanStyle(
                                        textDecoration = TextDecoration.Underline,
                                        color = Color.White
                                    )
                                ) {
                                    append("Регистрация")
                                }
                            },

                            fontSize = 14.sp,
                            color = Color.White,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.Register.route)
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
fun LoginScreenPreview() {
    val navController = rememberNavController()
    LoginScreen(navController)
}