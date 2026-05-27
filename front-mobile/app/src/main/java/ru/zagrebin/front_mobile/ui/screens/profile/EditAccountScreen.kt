package ru.zagrebin.front_mobile.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.activity.result.PickVisualMediaRequest
import coil.compose.AsyncImage
import ru.zagrebin.front_mobile.ui.theme.AppPageBackgroundColor
import ru.zagrebin.front_mobile.ui.theme.LightPrimary
import java.io.File

@Composable
fun EditAccountScreen(
    initialName: String = "",
    initialAvatarUrl: String? = null,
    onBackClick: () -> Unit,
    onSaveClick: (String, Uri?) -> Unit
) {
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf(initialName) }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var cropScale by rememberSaveable { mutableStateOf(1f) }
    var cropOffsetX by rememberSaveable { mutableStateOf(0f) }
    var cropOffsetY by rememberSaveable { mutableStateOf(0f) }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            avatarUri = uri
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            avatarUri = pendingCameraUri
        }
    }

    val trimmedName = name.trim()

    LaunchedEffect(avatarUri) {
        cropScale = 1f
        cropOffsetX = 0f
        cropOffsetY = 0f
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppPageBackgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EditAccountTopBar(onBackClick = onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(124.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        val displayModel = avatarUri ?: initialAvatarUrl
                        if (displayModel != null) {
                            AsyncImage(
                                model = displayModel,
                                contentDescription = "Аватар",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .pointerInput(displayModel) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            cropOffsetX += dragAmount.x
                                            cropOffsetY += dragAmount.y
                                        }
                                    }
                                    .graphicsLayer(
                                        scaleX = cropScale,
                                        scaleY = cropScale,
                                        translationX = cropOffsetX,
                                        translationY = cropOffsetY
                                    )
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = Color(0xFF8A8A8A),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    if (avatarUri != null) {
                        Text(
                            text = "Перемещайте фото и меняйте масштаб",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8A8A8A)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Масштаб",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8A8A8A)
                            )
                            Slider(
                                value = cropScale,
                                onValueChange = { cropScale = it },
                                valueRange = 1f..3f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                pickMediaLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Image,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Из галереи")
                        }

                        Button(
                            onClick = {
                                val uri = createTempImageUri(context)
                                pendingCameraUri = uri
                                takePictureLauncher.launch(uri)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1C1F))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Сделать фото", color = Color.White)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Имя пользователя",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2A2A2A)
                    )
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        supportingText = {
                            Text(text = "Оставьте пустым, если не хотите указывать", color = Color(0xFF8A8A8A))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            onSaveClick(trimmedName, avatarUri)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightPrimary)
                    ) {
                        Text(
                            text = "Сохранить",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 36.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditAccountTopBar(onBackClick: () -> Unit) {
    Surface(color = AppPageBackgroundColor) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад"
                )
            }
            Text(
                text = "Редактировать аккаунт",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

private fun createTempImageUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images")
    if (!imagesDir.exists()) {
        imagesDir.mkdirs()
    }
    val file = File.createTempFile("avatar_", ".jpg", imagesDir)
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, file)
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun EditAccountScreenPreview() {
    EditAccountScreen(
        initialName = "Иван Иванов",
        initialAvatarUrl = null,
        onBackClick = {},
        onSaveClick = { _, _ -> }
    )
}
