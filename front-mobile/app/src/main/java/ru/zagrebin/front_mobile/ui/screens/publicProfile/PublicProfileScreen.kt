package ru.zagrebin.front_mobile.ui.screens.publicProfile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardContent
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState

@Composable
fun PublicProfileScreen(
    userId: String,
    onBackClick: () -> Unit,
    onOpenRecipe: (Int) -> Unit,
    viewModel: PublicProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(userId) {
        viewModel.load(userId)
    }

    PublicProfileContent(
        state = state,
        onBackClick = onBackClick,
        onToggleFollow = viewModel::toggleFollow,
        onTagClick = { _, _ -> },
        onOpenRecipe = onOpenRecipe
    )
}

@Composable
private fun PublicProfileContent(
    state: PublicProfileUiState,
    onBackClick: () -> Unit,
    onToggleFollow: () -> Unit,
    onTagClick: (Int, Int) -> Unit,
    onOpenRecipe: (Int) -> Unit
) {
    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F3F3)),
        contentPadding = PaddingValues(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            PublicProfileHeader(
                state = state,
                onBackClick = onBackClick,
                onToggleFollow = onToggleFollow
            )
        }

        if (state.error != null) {
            item {
                Text(
                    text = state.error,
                    color = Color.Red,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        items(state.posts, key = { post -> post.id }) { post ->
            PostCardContent(
                state = post,
                onTagClick = { tagId -> onTagClick(post.id, tagId) },
                onOpenRecipe = onOpenRecipe
            )
        }
    }
}

@Composable
private fun PublicProfileHeader(
    state: PublicProfileUiState,
    onBackClick: () -> Unit,
    onToggleFollow: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF917C6A),
                        Color(0xFF2C1A12)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileHeaderStat(
                    value = state.followingCount,
                    title = "Подписки",
                    modifier = Modifier.padding(start = 24.dp)
                )

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFC8B39F))
                        .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(46.dp)
                    )
                }

                ProfileHeaderStat(
                    value = state.followersCount,
                    title = "Подписчики",
                    modifier = Modifier.padding(end = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = state.name,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = state.email,
                color = Color.White.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(14.dp))

            val buttonText = if (state.isFollowing) "Вы подписаны" else "Подписаться"
            val buttonColor = if (state.isFollowing) Color.White.copy(alpha = 0.2f) else Color.Transparent

            Button(
                onClick = onToggleFollow,
                enabled = !state.isFollowUpdating,
                modifier = Modifier
                    .width(240.dp)
                    .height(35.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.White,
                    disabledContainerColor = buttonColor,
                    disabledContentColor = Color.White.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = if (state.isFollowUpdating) "Обновляем..." else buttonText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ProfileHeaderStat(
    value: String,
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.78f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun PublicProfileScreenPreview() {
    PublicProfileContent(
        state = PublicProfileUiState(
            userId = "42",
            name = "Дмитрий Загребин",
            email = "dmitry.zagrebin@gmail.com",
            followingCount = "828",
            followersCount = "72.9k",
            isFollowing = false,
            posts = listOf(
                PostCardState(
                    id = 1,
                    authorName = "Дмитрий Загребин",
                    authorHandle = "@Dima123",
                    date = "24.03.2026",
                    title = "Токпокки (Tteokbokki) - классический рецепт",
                    imageUrl = "",
                    likes = "38.6k",
                    time = "35 мин",
                    calories = "250 ккал",
                    views = "53.7k"
                )
            )
        ),
        onBackClick = {},
        onToggleFollow = {},
        onTagClick = { _, _ -> },
        onOpenRecipe = {}
    )
}


