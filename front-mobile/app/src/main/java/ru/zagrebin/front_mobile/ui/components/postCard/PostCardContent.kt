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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagScreen

@Composable
fun PostCardContent(
    state: PostCardState,
    onTagClick: (Int) -> Unit,
    onOpenRecipe: (Int) -> Unit,
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

            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(enabled = state.authorId.isNotBlank()) {
                    onAuthorClick(state.authorId)
                }
            ) {

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD8C2A0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                }

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
                model = state.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(18.dp))
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Stat(Icons.Default.FavoriteBorder, state.likes)
                Stat(Icons.Default.Timer, state.time)
                Stat(Icons.Default.Whatshot, state.calories)
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
                onClick = { onOpenRecipe(state.id) },
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
                Text("Открыть рецепт", fontWeight = FontWeight.SemiBold)
            }
        }
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