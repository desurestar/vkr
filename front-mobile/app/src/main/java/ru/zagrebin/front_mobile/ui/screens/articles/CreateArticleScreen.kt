package ru.zagrebin.front_mobile.ui.screens.articles

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import ru.zagrebin.front_mobile.ui.common.asImageModelUrl
import ru.zagrebin.front_mobile.ui.theme.AppPageBackgroundColor
import ru.zagrebin.front_mobile.ui.theme.LightPrimary
import java.io.File
import kotlinx.coroutines.delay

private const val MAX_TAGS = 10
private const val TAG_SEARCH_DEBOUNCE_MS = 400L

@Composable
fun CreateArticleScreen(
    onBackClick: () -> Unit = {},
    availableTags: List<String> = listOf("Завтрак", "Обед", "Ужин", "ПП", "Веган"),
    initialDraft: ArticleEditDraft? = null,
    isEditMode: Boolean = false,
    onPublish: (title: String, summary: String, content: String, tags: List<String>, coverUri: Uri?, existingCoverUrl: String?, blocks: List<ArticleBlockDraft>) -> Unit = { _, _, _, _, _, _, _ -> },
    onDraft: (title: String, summary: String, content: String, tags: List<String>, coverUri: Uri?, existingCoverUrl: String?, blocks: List<ArticleBlockDraft>) -> Unit = { _, _, _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    var title by rememberSaveable(initialDraft?.id) { mutableStateOf(initialDraft?.title.orEmpty()) }
    var coverUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    val selectedTags = remember(initialDraft?.id) { mutableStateListOf<String>().apply { addAll(initialDraft?.tags.orEmpty()) } }
    val blocks = remember(initialDraft?.id) { mutableStateListOf<ArticleBlockDraft>().apply { addAll(initialDraft?.blocks.orEmpty()) } }

    var showTagSheet by rememberSaveable { mutableStateOf(false) }
    var showBlockSheet by rememberSaveable { mutableStateOf(false) }
    var nextBlockNumber by rememberSaveable(initialDraft?.id) { mutableStateOf((initialDraft?.blocks?.maxOfOrNull { it.number } ?: 0) + 1) }

    val pickCoverLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coverUri = uri
        }
    }

    val takeCoverLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            coverUri = pendingCameraUri
        }
    }

    val titleValid = title.trim().isNotEmpty()
    val tagsValid = selectedTags.isNotEmpty() && selectedTags.size <= MAX_TAGS
    val blocksValid = blocks.isNotEmpty()
    val isFormValid = titleValid && tagsValid && blocksValid

    if (showTagSheet) {
        TagPickBottomSheet(
            tags = availableTags.ifEmpty { listOf("Завтрак", "Обед", "Ужин", "ПП", "Веган") },
            selected = selectedTags,
            onDismiss = { showTagSheet = false },
            onAddClick = { showTagSheet = false }
        )
    }

    if (showBlockSheet) {
        ArticleBlockAddBottomSheet(
            onDismiss = { showBlockSheet = false },
            onAddClick = { blockTitle, blockContent, photoUri ->
                blocks.add(
                    ArticleBlockDraft(
                        number = nextBlockNumber,
                        title = blockTitle,
                        content = blockContent,
                        photoUri = photoUri
                    )
                )
                nextBlockNumber += 1
                showBlockSheet = false
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppPageBackgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            CreateArticleTopBar(onBackClick = onBackClick, isEditMode = isEditMode)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Заголовок статьи",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6F6F6F)
                )

                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Заголовок статьи...") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showErrors && !titleValid,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = inputColors(),
                    supportingText = {
                        if (showErrors && !titleValid) {
                            Text(text = "Введите заголовок статьи")
                        }
                    }
                )

                ArticleCoverBlock(
                    coverUri = coverUri,
                    existingCoverUrl = initialDraft?.coverUrl,
                    onPickGallery = {
                        pickCoverLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onTakePhoto = {
                        val uri = createTempImageUri(context, "article_")
                        pendingCameraUri = uri
                        takeCoverLauncher.launch(uri)
                    }
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showTagSheet = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Добавить тег")
                    }
                    TagRow(tags = selectedTags, onRemove = { selectedTags.remove(it) })
                }

                if (showErrors && !tagsValid) {
                    val errorText = if (selectedTags.isEmpty()) {
                        "Добавьте хотя бы один тег"
                    } else {
                        "Можно выбрать не более $MAX_TAGS тегов"
                    }
                    Text(
                        text = errorText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD32F2F)
                    )
                }

                ArticleBlocksBlock(
                    blocks = blocks,
                    onAddClick = { showBlockSheet = true },
                    onRemoveBlock = { blocks.remove(it) }
                )

                if (showErrors && !blocksValid) {
                    Text(
                        text = "Добавьте хотя бы один блок",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD32F2F)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val normalizedBlocks = blocks.toList()
                            val summary = normalizedBlocks.firstOrNull()?.content.orEmpty().take(180)
                            val content = normalizedBlocks.joinToString("\n\n") { block ->
                                listOf(
                                    "## ${block.title}",
                                    block.content
                                ).joinToString("\n")
                            }
                            onDraft(
                                title.trim().ifBlank { "Черновик статьи" },
                                summary,
                                content,
                                selectedTags.toList(),
                                coverUri,
                                initialDraft?.coverUrl,
                                normalizedBlocks
                            )
                        },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isEditMode) "Сохранить черновик" else "Черновик")
                    }
                    Button(
                        onClick = {
                            showErrors = true
                            if (!isFormValid) return@Button
                            val normalizedBlocks = blocks.toList()
                            val summary = normalizedBlocks.firstOrNull()?.content.orEmpty().take(180)
                            val content = normalizedBlocks.joinToString("\n\n") { block ->
                                listOf(
                                    "## ${block.title}",
                                    block.content
                                ).joinToString("\n")
                            }
                            onPublish(
                                title.trim(),
                                summary,
                                content,
                                selectedTags.toList(),
                                coverUri,
                                initialDraft?.coverUrl,
                                normalizedBlocks
                            )
                        },
                        enabled = isFormValid,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF6C166)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isEditMode) "Сохранить" else "Опубликовать", color = Color(0xFF1E1C1F))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun CreateArticleTopBar(onBackClick: () -> Unit, isEditMode: Boolean) {
    Surface(color = AppPageBackgroundColor) {
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
                text = if (isEditMode) "Редактировать статью" else "Создать статью",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun ArticleCoverBlock(
    coverUri: Uri?,
    existingCoverUrl: String?,
    onPickGallery: () -> Unit,
    onTakePhoto: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color(0xFFE3E3E3), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (coverUri != null || !existingCoverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = coverUri ?: existingCoverUrl.asImageModelUrl(),
                    contentDescription = "Обложка статьи",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = Color(0xFF7A7A7A),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(text = "Добавить обложку", color = Color(0xFF7A7A7A))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onPickGallery,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Image, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Из галереи")
            }
            Button(
                onClick = onTakePhoto,
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
}

@Composable
private fun inputColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent
)

private fun createTempImageUri(context: Context, prefix: String): Uri {
    val imagesDir = File(context.cacheDir, "images")
    if (!imagesDir.exists()) {
        imagesDir.mkdirs()
    }
    val file = File.createTempFile(prefix, ".jpg", imagesDir)
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, file)
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun CreateArticleScreenPreview() {
    CreateArticleScreen()
}

@Composable
private fun TagRow(
    tags: List<String>,
    onRemove: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tags.forEach { tag ->
            Surface(
                color = Color(0xFFEDE6DE),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = tag, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Удалить тег",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onRemove(tag) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleBlocksBlock(
    blocks: List<ArticleBlockDraft>,
    onAddClick: () -> Unit,
    onRemoveBlock: (ArticleBlockDraft) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Блоки статьи",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Добавить блок")
            }
        }
        blocks.forEach { block ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Блок ${block.number}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Удалить блок",
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onRemoveBlock(block) }
                        )
                    }
                    Text(text = block.title, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = block.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4A4A4A)
                    )
                    if (block.photoUri != null || !block.existingImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = block.photoUri ?: block.existingImageUrl.asImageModelUrl(),
                            contentDescription = "Фото блока",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .background(Color(0xFFEDEDED), RoundedCornerShape(10.dp))
                        )
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun TagPickBottomSheet(
    tags: List<String>,
    selected: MutableList<String>,
    onDismiss: () -> Unit,
    onAddClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val draftSelected = remember { mutableStateListOf<String>().apply { addAll(selected) } }
    var tagQuery by rememberSaveable { mutableStateOf("") }
    var debouncedTagQuery by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(tagQuery) {
        delay(TAG_SEARCH_DEBOUNCE_MS)
        debouncedTagQuery = tagQuery
    }

    val filteredTags = remember(debouncedTagQuery, tags, draftSelected.size) {
        val query = debouncedTagQuery.trim()
        val availableTags = tags.filterNot { draftSelected.contains(it) }
        if (query.isEmpty()) {
            availableTags
        } else {
            availableTags.filter { it.contains(query, ignoreCase = true) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxHeight(0.92f),
        containerColor = Color(0xFFF7F5F2),
        scrimColor = Color.Black.copy(alpha = 0.32f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Выберите теги",
                style = MaterialTheme.typography.titleMedium
            )
            TextField(
                value = tagQuery,
                onValueChange = { tagQuery = it },
                placeholder = { Text("Поиск тега...") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = inputColors()
            )

            Text(
                text = "Выбранные теги",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (draftSelected.isEmpty()) {
                Text(
                    text = "Пока ничего не выбрано",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8A8A8A)
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    draftSelected.forEach { tag ->
                        Surface(
                            color = LightPrimary.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable { draftSelected.remove(tag) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightPrimary
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Удалить тег",
                                    tint = LightPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "Результаты поиска",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                if (filteredTags.isEmpty()) {
                    item {
                        Text(
                            text = "Подходящих тегов не найдено",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8A8A8A),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(filteredTags) { tag ->
                        val canAddMore = draftSelected.size < MAX_TAGS
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .clickable(enabled = canAddMore) {
                                    if (!draftSelected.contains(tag)) draftSelected.add(tag)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = false,
                                enabled = canAddMore,
                                onCheckedChange = { value ->
                                    if (value && !draftSelected.contains(tag)) draftSelected.add(tag)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = LightPrimary)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = tag, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            if (draftSelected.size >= MAX_TAGS) {
                Text(
                    text = "Лимит: не более $MAX_TAGS тегов",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD32F2F)
                )
            }
            Button(
                onClick = {
                    selected.clear()
                    selected.addAll(draftSelected)
                    onAddClick()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Применить", color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun ArticleBlockAddBottomSheet(
    onDismiss: () -> Unit,
    onAddClick: (String, String, Uri?) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var blockTitle by rememberSaveable { mutableStateOf("") }
    var blockContent by rememberSaveable { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingBlockCameraUri by remember { mutableStateOf<Uri?>(null) }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photoUri = uri
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri = pendingBlockCameraUri
        }
    }

    val titleValid = blockTitle.trim().isNotEmpty()
    val contentValid = blockContent.trim().isNotEmpty()
    val canSubmit = titleValid && contentValid

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF7F5F2),
        scrimColor = Color.Black.copy(alpha = 0.32f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Добавить блок",
                style = MaterialTheme.typography.titleMedium
            )
            TextField(
                value = blockTitle,
                onValueChange = { blockTitle = it },
                placeholder = { Text("Заголовок блока...") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = inputColors()
            )
            TextField(
                value = blockContent,
                onValueChange = { blockContent = it },
                placeholder = { Text("Содержание блока...") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                singleLine = false,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = inputColors()
            )
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Фото блока",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color(0xFFEDEDED), RoundedCornerShape(10.dp))
                )
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
                    Icon(imageVector = Icons.Outlined.Image, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Из галереи")
                }
                Button(
                    onClick = {
                        val uri = createTempImageUri(context, "article_block_")
                        pendingBlockCameraUri = uri
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
            Button(
                onClick = {
                    if (!canSubmit) return@Button
                    onAddClick(blockTitle.trim(), blockContent.trim(), photoUri)
                },
                enabled = canSubmit,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Добавить", color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

data class ArticleBlockDraft(
    val number: Int,
    val title: String,
    val content: String,
    val photoUri: Uri?,
    val existingImageUrl: String? = null
)

data class ArticleEditDraft(
    val id: Int,
    val title: String,
    val coverUrl: String?,
    val tags: List<String>,
    val blocks: List<ArticleBlockDraft>
)
