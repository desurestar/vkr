package ru.zagrebin.front_mobile.ui.screens.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.zagrebin.front_mobile.ui.common.rememberExplicitCacheImageRequest
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.screens.recipe.RecipeCommentUi
import ru.zagrebin.front_mobile.ui.screens.recipe.RecipeCommentsBottomSheet
import ru.zagrebin.front_mobile.ui.screens.recipe.RecipeCommentsButton
import ru.zagrebin.front_mobile.ui.theme.AppPageBackgroundColor

@Composable
fun ArticleDetailsScreen(
    article: PostCardState,
    content: String,
    onBackClick: () -> Unit
) {
    var showComments by rememberSaveable { mutableStateOf(false) }
    val comments = remember { mutableStateListOf<RecipeCommentUi>() }

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
                        .aspectRatio(1.6f)
                        .clip(RoundedCornerShape(16.dp))
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
                comments.add(
                    RecipeCommentUi(
                        id = (comments.size + 1).toString(),
                        authorName = "Вы",
                        authorHandle = "@you",
                        date = "сейчас",
                        text = text,
                        replyToName = replyTo?.authorName
                    )
                )
            }
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
                        .aspectRatio(1.6f)
                        .clip(RoundedCornerShape(14.dp))
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
