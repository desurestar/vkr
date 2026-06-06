package ru.zagrebin.front_mobile.ui.screens.publicProfile

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardContent
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.theme.SearchFieldContainerColor
import ru.zagrebin.front_mobile.ui.theme.SearchFieldCornerRadius
import ru.zagrebin.front_mobile.ui.common.asImageModelUrl

@Composable
fun PublicProfileScreen(
    userId: String,
    onBackClick: () -> Unit,
    onOpenRecipe: (Int) -> Unit,
    onOpenArticle: (Int) -> Unit,
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
        onLikeClick = viewModel::toggleLike,
        onSearch = viewModel::onSearch,
        onTagClick = { _, _ -> },
        onOpenRecipe = onOpenRecipe,
        onOpenArticle = onOpenArticle
    )
}

private enum class PublicProfileTab {
    Recipes,
    Articles
}

@Composable
private fun PublicProfileContent(
    state: PublicProfileUiState,
    onBackClick: () -> Unit,
    onToggleFollow: () -> Unit,
    onLikeClick: (Int) -> Unit,
    onSearch: (String) -> Unit,
    onTagClick: (Int, Int) -> Unit,
    onOpenRecipe: (Int) -> Unit,
    onOpenArticle: (Int) -> Unit
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

    var selectedTab by rememberSaveable { mutableStateOf(PublicProfileTab.Recipes) }
    val visiblePosts = state.posts.filter { post ->
        when (selectedTab) {
            PublicProfileTab.Recipes -> post.type.equals("RECIPE", ignoreCase = true)
            PublicProfileTab.Articles -> post.type.equals("ARTICLE", ignoreCase = true)
        }
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

        item {
            PublicProfileTabSelector(
                selectedTab = selectedTab,
                onTabChange = { selectedTab = it }
            )
        }

        item {
            PublicProfileSearchField(
                value = state.searchQuery,
                onValueChange = onSearch
            )
        }

        if (state.isPostsLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (visiblePosts.isEmpty()) {
            item {
                EmptyPublicProfilePosts(tab = selectedTab)
            }
        } else {
            items(visiblePosts, key = { post -> post.id }) { post ->
                val isArticle = post.type.equals("ARTICLE", ignoreCase = true)
                PostCardContent(
                    state = post,
                    onTagClick = { tagId -> onTagClick(post.id, tagId) },
                    onOpenRecipe = { postId -> if (isArticle) onOpenArticle(postId) else onOpenRecipe(postId) },
                    actionText = if (isArticle) "Открыть статью" else "Открыть рецепт",
                    onLikeClick = { onLikeClick(post.id) },
                    onAuthorClick = {}
                )
            }
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
                    if (state.avatarUrl.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(46.dp)
                        )
                    } else {
                        AsyncImage(
                            model = state.avatarUrl.asImageModelUrl(),
                            contentDescription = "Аватар пользователя",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
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
                text = state.handle,
                color = Color.White.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(14.dp))

            val buttonText = if (state.isFollowing) "Вы подписаны" else "Подписаться"
            val buttonColor = if (state.isFollowing) Color.White.copy(alpha = 0.2f) else Color.Transparent

            Button(
                onClick = onToggleFollow,
                enabled = !state.isFollowUpdating && !state.isOwnProfile,
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
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = when {
                        state.isOwnProfile -> "Это ваш профиль"
                        state.isFollowUpdating -> "Обновляем..."
                        else -> buttonText
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PublicProfileTabSelector(
    selectedTab: PublicProfileTab,
    onTabChange: (PublicProfileTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PublicProfileTabButton(
            text = "Рецепты",
            selected = selectedTab == PublicProfileTab.Recipes,
            onClick = { onTabChange(PublicProfileTab.Recipes) },
            modifier = Modifier.weight(1f)
        )
        PublicProfileTabButton(
            text = "Статьи",
            selected = selectedTab == PublicProfileTab.Articles,
            onClick = { onTabChange(PublicProfileTab.Articles) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PublicProfileTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF2C1A12) else Color.White,
            contentColor = if (selected) Color.White else Color(0xFF2C1A12)
        ),
        border = if (selected) null else BorderStroke(1.dp, Color(0xFFE0D8CF)),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PublicProfileSearchField(
    value: String,
    onValueChange: (String) -> Unit
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        placeholder = { Text("Поиск по названию") },
        singleLine = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Искать"
            )
        },
        shape = RoundedCornerShape(SearchFieldCornerRadius),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SearchFieldContainerColor,
            unfocusedContainerColor = SearchFieldContainerColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun EmptyPublicProfilePosts(tab: PublicProfileTab) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Text(
            text = if (tab == PublicProfileTab.Recipes) "У пользователя пока нет рецептов" else "У пользователя пока нет статей",
            modifier = Modifier.padding(20.dp),
            color = Color.Gray,
            style = MaterialTheme.typography.bodyLarge
        )
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
            handle = "@Dima123",
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
        onLikeClick = {},
        onSearch = {},
        onTagClick = { _, _ -> },
        onOpenRecipe = {},
        onOpenArticle = {}
    )
}


