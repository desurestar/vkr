package ru.zagrebin.front_mobile.ui.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.zagrebin.front_mobile.ui.common.rememberExplicitCacheImageRequest

@Composable
fun RecipeCommentsButton(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF5E5E5E),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Комментарии • $count",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = Color(0xFF5E5E5E)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCommentsBottomSheet(
    comments: List<RecipeCommentUi>,
    onDismiss: () -> Unit,
    onSendClick: (String, RecipeCommentUi?) -> Unit,
    onDeleteClick: (RecipeCommentUi) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var replyTo by remember { mutableStateOf<RecipeCommentUi?>(null) }
    val threadedComments = remember(comments) { comments.asThreadedItems() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF7F5F2),
        scrimColor = Color.Black.copy(alpha = 0.32f)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .imePadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .background(Color(0xFFD0D0D0), RoundedCornerShape(2.dp))
                )
            }

            HorizontalDivider(color = Color(0xFFE1E1E1))

            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Пока нет комментариев",
                        color = Color(0xFF8E8E8E),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(threadedComments, key = { it.comment.id }) { threadItem ->
                        RecipeCommentItem(
                            comment = threadItem.comment,
                            nestingLevel = threadItem.nestingLevel,
                            onReplyClick = { replyTo = threadItem.comment },
                            onDeleteClick = { onDeleteClick(threadItem.comment) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }

            HorizontalDivider(color = Color(0xFFE1E1E1))

            Column(modifier = Modifier.fillMaxWidth()) {
                if (replyTo != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ответ: ${replyTo?.authorName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8E8E8E),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Отмена",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8E8E8E),
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clickable { replyTo = null }
                                .padding(4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Написать комментарий...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = {
                            val text = inputText.trim()
                            if (text.isNotEmpty()) {
                                onSendClick(text, replyTo)
                                inputText = ""
                                replyTo = null
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF7B5C4D), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Отправить",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeCommentItem(
    comment: RecipeCommentUi,
    nestingLevel: Int,
    onReplyClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val startPadding = (nestingLevel.coerceAtMost(MaxCommentNestingLevel) * ReplyIndentDp).dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding),
        verticalAlignment = Alignment.Top
    ) {
        CommentAvatar(comment = comment)

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.authorName,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = comment.authorHandle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8E8E8E)
                )
            }

            if (comment.replyToName != null) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(Color(0xFFF1E7DE), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Ответ: ${comment.replyToName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8E8E8E)
                    )
                }
            }

            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF3A3A3A)
            )

            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8E8E8E)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Ответить",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8E8E8E),
                    modifier = Modifier
                        .background(Color.Transparent)
                        .padding(2.dp)
                        .clickable { onReplyClick() }
                )
                if (comment.canDelete) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить комментарий",
                        tint = Color(0xFFB36A5E),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onDeleteClick() }
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentAvatar(comment: RecipeCommentUi) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(comment.avatarColor),
        contentAlignment = Alignment.Center
    ) {
        val imageRequest = rememberExplicitCacheImageRequest(comment.authorAvatarUrl)

        Text(
            text = comment.authorName.take(1),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge
        )

        if (imageRequest != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = "Аватар пользователя ${comment.authorName}",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

data class RecipeCommentUi(
    val id: String,
    val serverId: Long,
    val authorId: Long,
    val authorName: String,
    val authorHandle: String,
    val date: String,
    val text: String,
    val authorAvatarUrl: String? = null,
    val parentId: Long? = null,
    val replyToName: String? = null,
    val canDelete: Boolean = false,
    val avatarColor: Color = Color(0xFFD2B091)
)

private const val ReplyIndentDp = 28
private const val MaxCommentNestingLevel = 2

private data class RecipeCommentThreadItem(
    val comment: RecipeCommentUi,
    val nestingLevel: Int
)

private fun List<RecipeCommentUi>.asThreadedItems(): List<RecipeCommentThreadItem> {
    if (isEmpty()) return emptyList()

    val commentsById = associateBy { it.serverId }
    val commentsByParent = groupBy { it.parentId }
    val visited = mutableSetOf<Long>()
    val threadedItems = mutableListOf<RecipeCommentThreadItem>()

    fun append(comment: RecipeCommentUi, nestingLevel: Int) {
        if (!visited.add(comment.serverId)) return

        threadedItems += RecipeCommentThreadItem(
            comment = comment,
            nestingLevel = nestingLevel.coerceAtMost(MaxCommentNestingLevel)
        )

        commentsByParent[comment.serverId].orEmpty().forEach { child ->
            append(child, nestingLevel + 1)
        }
    }

    filter { comment -> comment.parentId == null || commentsById[comment.parentId] == null }
        .forEach { rootComment -> append(rootComment, nestingLevel = 0) }

    forEach { comment -> append(comment, nestingLevel = 0) }

    return threadedItems
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun RecipeCommentsButtonPreview() {
    RecipeCommentsButton(count = 12, onClick = {})
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun RecipeCommentsBottomSheetPreview() {
    val sample = listOf(
        RecipeCommentUi(
            id = "1",
            serverId = 1,
            authorId = 1,
            authorName = "Лилия Парк",
            authorHandle = "@LiliaSpicyTok",
            date = "24.03.2026",
            text = "Ого, выглядит просто супер! Это точно другой уровень корейской кухни!",
            avatarColor = Color(0xFF9BCB90)
        ),
        RecipeCommentUi(
            id = "2",
            serverId = 2,
            authorId = 2,
            authorName = "Дмитрий Загребин",
            authorHandle = "@Dim123",
            date = "24.03.2026",
            text = "Привет, Оля! Рад, что рецепт понравился! Можно экспериментировать с остротой.",
            replyToName = "Лилия Парк",
            avatarColor = Color(0xFFC4A991)
        )
    )

    RecipeCommentsBottomSheet(comments = sample, onDismiss = {}, onSendClick = { _, _ -> }, onDeleteClick = {})
}
