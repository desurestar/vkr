package ru.zagrebin.front_mobile.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.theme.AppPageBackgroundColor
import ru.zagrebin.front_mobile.ui.theme.ListBottomPadding

@Composable
fun DraftsScreen(
    onBackClick: () -> Unit,
    onOpenRecipe: (Int) -> Unit,
    onOpenArticle: (Int) -> Unit
) {
    val viewModel: DraftsViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppPageBackgroundColor)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 72.dp, start = 16.dp, end = 16.dp, bottom = ListBottomPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.errorMessage?.let { message ->
                item {
                    ErrorBlock(message = message, onRetry = viewModel::loadDrafts)
                }
            }

            when {
                state.isLoading -> item { EmptyDraftsText("Загрузка черновиков...") }
                state.drafts.isEmpty() -> item { EmptyDraftsText("Черновиков пока нет") }
                else -> items(state.drafts, key = { it.id }) { draft ->
                    DraftCard(
                        draft = draft,
                        onOpen = {
                            if (draft.id > 0) {
                                if (draft.type == "ARTICLE") onOpenArticle(draft.id) else onOpenRecipe(draft.id)
                            }
                        },
                        onDelete = { viewModel.deleteDraft(draft.id) }
                    )
                }
            }
        }

        DraftsTopBar(
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun DraftCard(
    draft: PostCardState,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = when {
                        draft.id < 0 -> if (draft.type == "ARTICLE") "Статья · локально" else "Рецепт · локально"
                        draft.type == "ARTICLE" -> "Статья"
                        else -> "Рецепт"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF7C3AED)
                )
                Text(
                    text = draft.title.ifBlank { "Без названия" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = draft.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8E8E8E)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Удалить черновик",
                    tint = Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Composable
private fun DraftsTopBar(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(color = Color.White, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Text(text = "Черновики", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun ErrorBlock(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF3E0), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(message, color = Color(0xFF8A4B00))
        Button(onClick = onRetry) { Text("Повторить") }
    }
}

@Composable
private fun EmptyDraftsText(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(Color(0xFFF1E7DE), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("0", style = MaterialTheme.typography.titleLarge, color = Color(0xFF8E8E8E))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text, color = Color(0xFF8E8E8E))
    }
}
