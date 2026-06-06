package ru.zagrebin.front_mobile.ui.screens.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.zagrebin.front_mobile.domain.model.PostComment
import ru.zagrebin.front_mobile.ui.common.rememberExplicitCacheImageRequest
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.screens.recipe.RecipeCommentUi
import ru.zagrebin.front_mobile.ui.screens.recipe.RecipeCommentsBottomSheet
import ru.zagrebin.front_mobile.ui.screens.recipe.RecipeCommentsButton
import ru.zagrebin.front_mobile.ui.theme.AppPageBackgroundColor
import ru.zagrebin.front_mobile.ui.common.asImageModelUrl

@Composable
fun ArticleDetailsScreen(
    article: PostCardState,
    content: String,
    currentUserId: Long?,
    isAuthorized: Boolean = true,
    onAuthRequired: () -> Unit = {},
    onBackClick: () -> Unit,
    onSendComment: (String, Long?) -> Unit,
    onDeleteComment: (Long) -> Unit
) {
    var showComments by rememberSaveable { mutableStateOf(false) }
    val comments = remember(article.comments, currentUserId) { article.comments.toCommentUi(currentUserId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppPageBackgroundColor)
    ) {
        ArticleTopBar(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AuthorHeader(article = article)

            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            if (article.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = rememberExplicitCacheImageRequest(article.imageUrl),
                    contentDescription = article.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            ArticleContent(
                content = if (content.isBlank()) {
                    "Полный текст статьи будет здесь. Добавьте описание, шаги или рекомендации."
                } else {
                    content
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RecipeCommentsButton(
                count = comments.size,
                onClick = { showComments = true },
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }

    if (showComments) {
        RecipeCommentsBottomSheet(
            comments = comments,
            onDismiss = { showComments = false },
            onSendClick = { text, replyTo ->
                if (isAuthorized) onSendComment(text, replyTo?.serverId) else onAuthRequired()
            },
            onDeleteClick = { comment ->
                if (isAuthorized) onDeleteComment(comment.serverId) else onAuthRequired()
            }
        )
    }
}

private fun List<PostComment>.toCommentUi(currentUserId: Long?): List<RecipeCommentUi> = map { comment ->
    RecipeCommentUi(
        id = comment.id.toString(),
        serverId = comment.id,
        authorId = comment.authorId,
        authorName = comment.authorName.ifBlank { "Пользователь" },
        authorHandle = comment.authorHandle,
        date = comment.createdAt.ifBlank { "сейчас" },
        text = comment.text,
        replyToName = comment.parentAuthorName,
        canDelete = currentUserId != null && currentUserId == comment.authorId
    )
}

@Composable
private fun AuthorHeader(article: PostCardState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AuthorAvatar(
            authorName = article.authorName,
            avatarUrl = article.authorAvatarUrl
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(article.authorName, fontWeight = FontWeight.SemiBold)
            Text(
                article.authorHandle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = article.date,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AuthorAvatar(
    authorName: String,
    avatarUrl: String?
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0xFFD8C2A0)),
        contentAlignment = Alignment.Center
    ) {
        val model = avatarUrl.asImageModelUrl()
            ?: "https://ui-avatars.com/api/?background=D8C2A0&color=FFFFFF&name=${authorName.replace(" ", "+")}"

        AsyncImage(
            model = model,
            contentDescription = "Аватар автора",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ArticleContent(content: String) {
    val imagePattern = remember { Regex("^\\[image:(.+)]$") }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        content.split("\n").forEach { rawLine ->
            val line = rawLine.trim()
            val imageUrl = imagePattern.matchEntire(line)?.groupValues?.getOrNull(1)
            when {
                line.isBlank() -> Spacer(modifier = Modifier.height(2.dp))
                imageUrl != null -> AsyncImage(
                    model = rememberExplicitCacheImageRequest(imageUrl),
                    contentDescription = "Иллюстрация статьи",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
                line.startsWith("## ") -> Text(
                    text = line.removePrefix("## "),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF242424)
                )
                else -> Text(
                    text = line,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF3A3A3A)
                )
            }
        }
    }
}

@Composable
private fun ArticleTopBar(onBackClick: () -> Unit) {
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
            text = "Статья",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}
