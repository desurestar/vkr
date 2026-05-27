package ru.zagrebin.front_mobile.ui.screens.recipe

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.zagrebin.front_mobile.ui.theme.AppPageBackgroundColor
import ru.zagrebin.front_mobile.ui.theme.LightPrimary
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File

private const val MAX_TAGS = 10

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    onBackClick: () -> Unit = {},
    availableTags: List<String> = listOf("Завтрак", "Обед", "Ужин", "ПП", "Веган"),
    onPublish: (title: String, summary: String, content: String, cookTimeMinutes: Int, tags: List<String>, ingredients: List<IngredientDraft>, steps: List<RecipeStepDraft>, proteinsPer100: Double, fatsPer100: Double, carbsPer100: Double, kcalPer100: Double) -> Unit = { _, _, _, _, _, _, _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    var postTitle by rememberSaveable { mutableStateOf("") }
    var dishTitle by rememberSaveable { mutableStateOf("") }
    var recipePhotoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingRecipeCameraUri by remember { mutableStateOf<Uri?>(null) }
    var proteins by rememberSaveable { mutableStateOf("") }
    var fats by rememberSaveable { mutableStateOf("") }
    var carbs by rememberSaveable { mutableStateOf("") }
    var calories by rememberSaveable { mutableStateOf("") }
    var cookTimeMinutes by rememberSaveable { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    val selectedTags = remember { mutableStateListOf<String>() }
    val ingredients = remember { mutableStateListOf<IngredientDraft>() }
    val steps = remember { mutableStateListOf<RecipeStepDraft>() }

    var showTagSheet by rememberSaveable { mutableStateOf(false) }
    var showIngredientSheet by rememberSaveable { mutableStateOf(false) }
    var showStepSheet by rememberSaveable { mutableStateOf(false) }
    var nextStepNumber by rememberSaveable { mutableStateOf(1) }

    val pickRecipePhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            recipePhotoUri = uri
        }
    }

    val takeRecipePhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            recipePhotoUri = pendingRecipeCameraUri
        }
    }

    val postTitleValid = postTitle.trim().isNotEmpty()
    val dishTitleValid = dishTitle.trim().isNotEmpty()
    val nutrientsValid = listOf(proteins, fats, carbs, calories).all { it.toFloatOrNull() != null }
    val tagsValid = selectedTags.isNotEmpty() && selectedTags.size <= MAX_TAGS
    val ingredientsValid = ingredients.isNotEmpty()
    val stepsValid = steps.isNotEmpty()
    val cookTimeValid = cookTimeMinutes.toIntOrNull() != null
    val isFormValid = postTitleValid && dishTitleValid && nutrientsValid && cookTimeValid && tagsValid && ingredientsValid && stepsValid

    if (showTagSheet) {
        TagPickBottomSheet(
            tags = availableTags,
            selected = selectedTags,
            onDismiss = { showTagSheet = false },
            onAddClick = { showTagSheet = false }
        )
    }

    if (showIngredientSheet) {
        IngredientAddBottomSheet(
            onDismiss = { showIngredientSheet = false },
            onAddClick = { draft ->
                ingredients.add(draft)
                showIngredientSheet = false
            }
        )
    }

    if (showStepSheet) {
        StepAddBottomSheet(
            onDismiss = { showStepSheet = false },
            onAddClick = { description, photoUri ->
                steps.add(
                    RecipeStepDraft(
                        number = nextStepNumber,
                        description = description,
                        photoUri = photoUri
                    )
                )
                nextStepNumber += 1
                showStepSheet = false
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

            CreateRecipeTopBar(onBackClick = onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = "Заголовок поста",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6F6F6F)
                )

                TextField(
                    value = postTitle,
                    onValueChange = { postTitle = it },
                    placeholder = { Text("Заголовок поста...") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showErrors && !postTitleValid,
                    colors = inputColors(),
                    supportingText = {
                        if (showErrors && !postTitleValid) {
                            Text(text = "Введите заголовок поста")
                        }
                    }
                )

                RecipePhotoBlock(
                    photoUri = recipePhotoUri,
                    onPickGallery = {
                        pickRecipePhotoLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onTakePhoto = {
                        val uri = createTempImageUri(context, "recipe_")
                        pendingRecipeCameraUri = uri
                        takeRecipePhotoLauncher.launch(uri)
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
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text("Добавить тег")
                    }

                    TagRow(
                        tags = selectedTags,
                        onRemove = {
                            selectedTags.remove(it)
                        }
                    )
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

                Text(
                    text = "Описание рецепта",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                TextField(
                    value = dishTitle,
                    onValueChange = { dishTitle = it },
                    placeholder = { Text("Название блюда...") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showErrors && !dishTitleValid,
                    colors = inputColors(),
                    supportingText = {
                        if (showErrors && !dishTitleValid) {
                            Text(text = "Введите название блюда")
                        }
                    }
                )

                IngredientsBlock(
                    ingredients = ingredients,
                    onAddClick = { showIngredientSheet = true },
                    onRemoveIngredient = {
                        ingredients.remove(it)
                    }
                )

                if (showErrors && !ingredientsValid) {
                    Text(
                        text = "Добавьте хотя бы один ингредиент",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD32F2F)
                    )
                }

                StepsBlock(
                    steps = steps,
                    onAddClick = { showStepSheet = true },
                    onRemoveStep = { step ->
                        steps.remove(step)
                    }
                )

                if (showErrors && !stepsValid) {
                    Text(
                        text = "Добавьте хотя бы один шаг",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD32F2F)
                    )
                }

                TextField(
                    value = cookTimeMinutes,
                    onValueChange = { cookTimeMinutes = it },
                    placeholder = { Text("Время приготовления (мин)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = showErrors && !cookTimeValid,
                    colors = inputColors()
                )

                NutrientsBlock(
                    proteins = proteins,
                    fats = fats,
                    carbs = carbs,
                    calories = calories,
                    onProteinsChange = { proteins = it },
                    onFatsChange = { fats = it },
                    onCarbsChange = { carbs = it },
                    onCaloriesChange = { calories = it },
                    showErrors = showErrors && !nutrientsValid
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    OutlinedButton(
                        onClick = { },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Черновик")
                    }

                    Button(
                        onClick = {
                            showErrors = true
                            if (!isFormValid) return@Button
                            onPublish(
                                postTitle.trim(),
                                dishTitle.trim(),
                                steps.joinToString("\n") { it.description },
                                cookTimeMinutes.toInt(),
                                selectedTags.toList(),
                                ingredients.toList(),
                                steps.toList(),
                                proteins.toDouble(),
                                fats.toDouble(),
                                carbs.toDouble(),
                                calories.toDouble()
                            )
                        },
                        enabled = isFormValid,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF6C166)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Опубликовать",
                            color = Color(0xFF1E1C1F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun CreateRecipeTopBar(
    onBackClick: () -> Unit
) {
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
                text = "Создать пост",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun RecipePhotoBlock(
    photoUri: Uri?,
    onPickGallery: () -> Unit,
    onTakePhoto: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(
                    Color(0xFFE3E3E3),
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Фото рецепта",
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
                    Text(text = "Добавить фото", color = Color(0xFF7A7A7A))
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
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 6.dp
                    )
                ) {

                    Text(
                        text = tag,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Удалить тег",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                onRemove(tag)
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun IngredientsBlock(
    ingredients: List<IngredientDraft>,
    onAddClick: () -> Unit,
    onRemoveIngredient: (IngredientDraft) -> Unit
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Ингредиенты",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            OutlinedButton(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp)
            ) {

                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text("Добавить")
            }
        }

        ingredients.forEach { ingredient ->

            val amountText =
                if (ingredient.amount % 1f == 0f) {
                    ingredient.amount.toInt().toString()
                } else {
                    ingredient.amount.toString()
                }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "$amountText ${ingredient.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7A7A7A)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Удалить ингредиент",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                onRemoveIngredient(ingredient)
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun StepsBlock(
    steps: List<RecipeStepDraft>,
    onAddClick: () -> Unit,
    onRemoveStep: (RecipeStepDraft) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Шаги приготовления",
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
                Text("Добавить шаг")
            }
        }
        steps.forEach { step ->
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
                            text = "Шаг ${step.number}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Удалить шаг",
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onRemoveStep(step) }
                        )
                    }
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4A4A4A)
                    )
                    if (step.photoUri != null) {
                        AsyncImage(
                            model = step.photoUri,
                            contentDescription = "Фото шага",
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

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var tagQuery by rememberSaveable {
        mutableStateOf("")
    }

    val filteredTags = remember(tagQuery, tags) {

        val query = tagQuery.trim()

        if (query.isEmpty()) {
            tags
        } else {
            tags.filter {
                it.contains(query, ignoreCase = true)
            }
        }
    }

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

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 260.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 72.dp)
            ) {

                items(filteredTags) { tag ->

                    val checked = selected.contains(tag)
                    val canAddMore = selected.size < MAX_TAGS

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color.White,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Checkbox(
                            checked = checked,
                            enabled = checked || canAddMore,
                            onCheckedChange = { value ->

                                if (value) {
                                    if (checked || canAddMore) {
                                        if (!selected.contains(tag)) {
                                            selected.add(tag)
                                        }
                                    }
                                } else {
                                    selected.remove(tag)
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = LightPrimary
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = tag,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (selected.size >= MAX_TAGS) {
                Text(
                    text = "Лимит: не более $MAX_TAGS тегов",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD32F2F)
                )
            }

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "Добавить",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun IngredientAddBottomSheet(
    onDismiss: () -> Unit,
    onAddClick: (IngredientDraft) -> Unit
) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var name by rememberSaveable {
        mutableStateOf("")
    }

    var amount by rememberSaveable {
        mutableStateOf("")
    }

    var unit by rememberSaveable {
        mutableStateOf("г")
    }

    var expanded by remember {
        mutableStateOf(false)
    }

    val units = listOf(
        "г",
        "мл",
        "шт",
        "ст. л."
    )

    val amountValue = amount.toFloatOrNull() ?: 0f

    val canSubmit =
        name.trim().isNotEmpty() &&
                amountValue > 0f

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
                text = "Добавить ингредиент",
                style = MaterialTheme.typography.titleMedium
            )

            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Название") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = inputColors()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                TextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                            .replace(',', '.')
                            .filter { ch ->
                                ch.isDigit() || ch == '.'
                            }
                    },
                    placeholder = { Text("Количество") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    colors = inputColors()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {
                        expanded = !expanded
                    },
                    modifier = Modifier.weight(1f)
                ) {

                    TextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expanded
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = inputColors()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {

                        units.forEach { option ->

                            DropdownMenuItem(
                                text = {
                                    Text(option)
                                },
                                onClick = {
                                    unit = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {

                    if (!canSubmit) {
                        return@Button
                    }

                    onAddClick(
                        IngredientDraft(
                            name = name.trim(),
                            amount = amountValue,
                            unit = unit
                        )
                    )
                },
                enabled = canSubmit,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "Добавить",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun StepAddBottomSheet(
    onDismiss: () -> Unit,
    onAddClick: (String, Uri?) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var description by rememberSaveable { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

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
            photoUri = pendingCameraUri
        }
    }

    val canSubmit = description.trim().isNotEmpty()

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
                text = "Добавить шаг",
                style = MaterialTheme.typography.titleMedium
            )
            TextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Описание шага...") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = inputColors()
            )
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Фото шага",
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
                        val uri = createTempImageUri(context, "step_")
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
            Button(
                onClick = {
                    if (!canSubmit) return@Button
                    onAddClick(description.trim(), photoUri)
                },
                enabled = canSubmit,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Добавить",
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
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

data class IngredientDraft(
    val name: String,
    val amount: Float,
    val unit: String
)

data class RecipeStepDraft(
    val number: Int,
    val description: String,
    val photoUri: Uri?
)

@Preview(showBackground = true, locale = "ru")
@Composable
private fun CreateRecipeScreenPreview() {
    CreateRecipeScreen()
}

private fun createTempImageUri(context: Context, prefix: String): Uri {
    val imagesDir = File(context.cacheDir, "images")
    if (!imagesDir.exists()) {
        imagesDir.mkdirs()
    }
    val file = File.createTempFile(prefix, ".jpg", imagesDir)
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, file)
}

@Composable
private fun NutrientsBlock(
    proteins: String,
    fats: String,
    carbs: String,
    calories: String,
    onProteinsChange: (String) -> Unit,
    onFatsChange: (String) -> Unit,
    onCarbsChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    showErrors: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Нутриенты на 100 г",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(
                value = proteins,
                onValueChange = { onProteinsChange(it.toDecimalInput()) },
                placeholder = { Text("Белки, г") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = inputColors()
            )
            TextField(
                value = fats,
                onValueChange = { onFatsChange(it.toDecimalInput()) },
                placeholder = { Text("Жиры, г") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = inputColors()
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(
                value = carbs,
                onValueChange = { onCarbsChange(it.toDecimalInput()) },
                placeholder = { Text("Углеводы, г") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = inputColors()
            )
            TextField(
                value = calories,
                onValueChange = { onCaloriesChange(it.toDecimalInput()) },
                placeholder = { Text("Ккал") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = inputColors()
            )
        }
        if (showErrors) {
            Text(
                text = "Заполните все нутриенты (можно дробные)",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFD32F2F)
            )
        }
    }
}

private fun String.toDecimalInput(): String {
    val normalized = replace(',', '.')
    val firstDot = normalized.indexOf('.')
    return buildString {
        normalized.forEachIndexed { index, ch ->
            when {
                ch.isDigit() -> append(ch)
                ch == '.' && (firstDot == index) -> append(ch)
            }
        }
    }
}
