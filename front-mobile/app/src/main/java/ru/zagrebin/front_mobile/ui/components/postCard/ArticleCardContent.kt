package ru.zagrebin.front_mobile.ui.components.postCard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.zagrebin.front_mobile.ui.common.rememberExplicitCacheImageRequest
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagScreen
import ru.zagrebin.front_mobile.ui.common.asImageModelUrl

@Composable
fun ArticleCardContent(
    state: PostCardState,
    onTagClick: (Int) -> Unit,
    onOpenArticle: (Int) -> Unit,
    onLikeClick: () -> Unit = {},
    onAuthorClick: (String) -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(enabled = state.authorId.isNotBlank()) {
                    onAuthorClick(state.authorId)
                }
            ) {
                AuthorAvatar(
                    authorName = state.authorName,
                    avatarUrl = state.authorAvatarUrl
                )

                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f)) {
                    Text(state.authorName, fontWeight = FontWeight.SemiBold)
                    Text(state.authorHandle, color = Color.Gray)
                }

                Text(state.date, color = Color.Gray)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray)
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = state.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))

            AsyncImage(
                model = rememberExplicitCacheImageRequest(state.imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LikeStat(
                    likes = state.likes,
                    isLiked = state.isLiked,
                    onClick = onLikeClick
                )
                Stat(icon = Icons.Default.RemoveRedEye, text = state.views)
            }

            Spacer(Modifier.height(10.dp))

            FlowRow {
                state.tags.forEach { tag ->
                    TagScreen(
                        state = tag,
                        onClick = { onTagClick(tag.id) }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = { onOpenArticle(state.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF4F1EA),
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text("Открыть статью", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun LikeStat(
    likes: String,
    isLiked: Boolean,
    onClick: () -> Unit
) {
    val tint by animateColorAsState(
        targetValue = if (isLiked) Color(0xFFE53935) else Color.Gray,
        label = "likeColor"
    )
    val scale by animateFloatAsState(
        targetValue = if (isLiked) 1.18f else 1f,
        label = "likeScale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isLiked) "Убрать лайк" else "Поставить лайк",
            tint = tint,
            modifier = Modifier.scale(scale)
        )
        Spacer(Modifier.width(4.dp))
        Text(likes, color = tint, fontWeight = if (isLiked) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun Stat(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.Gray)
        Spacer(Modifier.width(4.dp))
        Text(text, color = Color.Gray)
    }
}



@Composable
private fun AuthorAvatar(
    authorName: String,
    avatarUrl: String?
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFFD8C2A0)),
        contentAlignment = Alignment.Center
    ) {
        val model = avatarUrl.asImageModelUrl() ?: "https://ui-avatars.com/api/?background=D8C2A0&color=FFFFFF&name=${
            authorName.replace(" ", "+")
        }"

        AsyncImage(
            model = model,
            contentDescription = "Аватар автора",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
    }
}
