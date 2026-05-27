package ru.zagrebin.front_mobile.ui.screens.profile.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import compose.icons.FeatherIcons
import compose.icons.feathericons.User

@Composable
fun ProfileHeader(
    name: String = "Иван Иванов",
    email: String = "test@email.ru",
    avatarUrl: String? = null,
    onCreateRecipeClick: () -> Unit = {},
    onCreateArticleClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(235.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(bottomEnd = 28.dp, bottomStart = 28.dp))
                .background(Color(0xFF1E1C1F))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 24.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    var expanded by remember { mutableStateOf(false) }

                    AnimatedContent(
                        targetState = expanded,
                        transitionSpec = {
                            // 🔥 ключевая часть — кастомная трансформация
                            (fadeIn() + scaleIn(initialScale = 0.8f)) togetherWith
                                    (fadeOut() + scaleOut(targetScale = 0.8f))
                        },
                        label = "create_transform"
                    ) { targetState ->

                        if (!targetState) {
                            // 🔹 КНОПКА
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3F3F3))
                                    .clickable { expanded = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "+",
                                    fontSize = 24.sp,
                                    color = Color.Black
                                )
                            }

                        } else {
                            // 🔹 КАРТОЧКА
                            Column(
                                modifier = Modifier
                                    .width(240.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFF3F3F3))
                                    .padding(16.dp)
                            ) {

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Создать",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { expanded = false }
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expanded = false
                                            onCreateRecipeClick()
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Description, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Пост")
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expanded = false
                                            onCreateArticleClick()
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Article, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Статья")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    modifier = Modifier.padding(start = 140.dp)
                ) {
                    Text(
                        text = name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = email,
                        color = Color.White.copy(0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.BottomStart)
                .offset(x = 24.dp, y = 20.dp)
                .clip(CircleShape)
                .background(Color(0xFFD6C2AE))
                .border(6.dp, Color(0xFFF3F3F3), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Аватар профиля",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = FeatherIcons.User,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }
        }

    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
fun ProfileHeaderPreview() {
    ProfileHeader()
}