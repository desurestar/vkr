package ru.zagrebin.front_mobile.ui.screens.entryOptions

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ru.zagrebin.front_mobile.R
import ru.zagrebin.front_mobile.ui.navigation.Screen

@Composable
fun EntryOptionsScreen(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.entry_options),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
            )

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) { }
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {},
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
                            Box(modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_yandex),
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .align(alignment = Alignment.CenterStart)
                                )

                                Text(text = "Войти с помощью Yandex",
                                    modifier = Modifier.align(Alignment.Center),
                                    fontSize = 16.sp,
                                    color = Color(0xFF1E1C1F).copy(0.4f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {},
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
                            Box(modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_vk),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .align(alignment = Alignment.CenterStart)
                                )

                                Text(text = "Войти с помощью VK",
                                    modifier = Modifier.align(Alignment.Center),
                                    fontSize = 16.sp,
                                    color = Color(0xFF1E1C1F).copy(0.4f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(50.dp))

                        Button(
                            onClick = {navController.navigate(Screen.Register.route)},
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
                            Box(modifier = Modifier.fillMaxSize()) {
                                Text(text = "Создать аккаунт",
                                    fontSize = 17.sp,
                                    color = Color(0xFF363536),
                                    modifier = Modifier.align(Alignment.Center),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {navController.navigate(Screen.Login.route)},
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFFFFF).copy(0.5f),      // фон кнопки
                                contentColor = Color(0xFF1E1C1F),              // цвет текста/иконок
                                disabledContainerColor = Color.Gray,     // фон, когда disabled
                                disabledContentColor = Color.LightGray   // текст, когда disabled
                            ),
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Text(text = "Войти",
                                    fontSize = 17.sp,
                                    color = Color(0xFFFFFFFF),
                                    modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
fun EntryOptionsScreenPreview() {
    val navController = rememberNavController()
    EntryOptionsScreen(navController)
}