package ru.zagrebin.front_mobile.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.zagrebin.front_mobile.ui.common.rememberExplicitCacheImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileUsersBottomSheet(
    type: ProfileUsersSheetType,
    query: String,
    users: List<ProfileUserItemState>,
    isLoading: Boolean,
    error: String?,
    onQueryChange: (String) -> Unit,
    onOpenUser: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = type.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Поиск пользователей") },
                singleLine = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Искать пользователей"
                    )
                },
                shape = RoundedCornerShape(18.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF3F3F3),
                    unfocusedContainerColor = Color(0xFFF3F3F3),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                error != null -> Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 18.dp)
                )

                users.isEmpty() -> Text(
                    text = type.emptyTitle,
                    color = Color(0xFF8A8A8A),
                    modifier = Modifier.padding(vertical = 18.dp)
                )

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(users, key = { it.id }) { user ->
                        ProfileUserRow(
                            user = user,
                            onClick = { onOpenUser(user.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileUserRow(
    user: ProfileUserItemState,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFD8C2A0)),
            contentAlignment = Alignment.Center
        ) {
            if (user.avatarUrl.isNullOrBlank()) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                AsyncImage(
                    model = rememberExplicitCacheImageRequest(user.avatarUrl),
                    contentDescription = "Аватар пользователя",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = user.displayName.ifBlank { "Пользователь" },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = user.username.withAtPrefix(),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8A8A8A)
            )
        }
    }
}

private fun String.withAtPrefix(): String = when {
    isBlank() -> "@user"
    startsWith("@") -> this
    else -> "@$this"
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun ProfileUsersBottomSheetPreview() {
    ProfileUserRow(
        user = ProfileUserItemState(
            id = 1,
            username = "dima123",
            displayName = "Дима"
        ),
        onClick = {}
    )
}
