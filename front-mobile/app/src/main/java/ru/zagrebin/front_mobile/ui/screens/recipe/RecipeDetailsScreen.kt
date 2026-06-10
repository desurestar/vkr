package ru.zagrebin.front_mobile.ui.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.common.rememberExplicitCacheImageRequest
import ru.zagrebin.front_mobile.ui.screens.statistics.AddMealBottomSheet
import ru.zagrebin.front_mobile.ui.screens.statistics.MealDraft
import ru.zagrebin.front_mobile.ui.screens.statistics.MealType
import ru.zagrebin.front_mobile.ui.theme.AppPageBackgroundColor
import ru.zagrebin.front_mobile.ui.theme.StepBadgeBackgroundColor
import ru.zagrebin.front_mobile.ui.theme.StepBadgeCornerRadius

@Composable
fun RecipeDetailsScreen(
    post: PostCardState,
    currentUserId: Long?,
    isAuthorized: Boolean = true,
    onAuthRequired: () -> Unit = {},
    onBackClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onSendComment: (String, Long?) -> Unit,
    onDeleteComment: (Long) -> Unit,
    onAddToShoppingList: (List<String>) -> Unit,
    onAddMeal: (MealType, MealDraft) -> Unit
) {
    var showComments by rememberSaveable { mutableStateOf(false) }
    var showIngredientsPicker by rememberSaveable { mutableStateOf(false) }
    var showAddMeal by rememberSaveable { mutableStateOf(false) }
    val comments = remember(post.comments, currentUserId) { post.comments.toCommentUi(currentUserId) }
    val ingredientOptions = remember(post.id) {
        post.ingredients.mapIndexed { index, ingredient ->
            IngredientPickUi(id = index, title = ingredient.text)
        }
    }
    val selectedIngredients = remember(post.id) {
        mutableStateMapOf<Int, Boolean>().apply {
            ingredientOptions.forEach { option ->
                this[option.id] = true
            }
        }
    }
    val recipeDraft = remember(post.id) {
        MealDraft(
            title = post.title,
            portionGrams = 100,
            isLiquid = false,
            proteinsPer100 = post.proteinsPer100,
            fatsPer100 = post.fatsPer100,
            carbsPer100 = post.carbsPer100,
            kcalPer100 = post.kcalPer100,
            recipeId = post.id
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppPageBackgroundColor),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            RecipeTopBar(
                canEdit = currentUserId != null && post.authorId == currentUserId.toString(),
                onBackClick = onBackClick,
                onEditClick = onEditClick,
                onEatClick = { if (isAuthorized) showAddMeal = true else onAuthRequired() }
            )
        }

        item {
            AuthorHeader(post = post)
        }

        item {
            Text(
                text = post.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            if (post.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = rememberExplicitCacheImageRequest(post.imageUrl),
                    contentDescription = post.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (post.description.isNotBlank()) {
            item {
                Text(
                    text = post.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF3A3A3A),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        item {
            IngredientsSection(
                post = post,
                onAddToListClick = { showIngredientsPicker = true }
            )
        }

        item {
            Text(
                text = "Процесс приготовления",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        itemsIndexed(post.steps, key = { _, step -> step.id }) { index, step ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(StepBadgeCornerRadius))
                        .background(StepBadgeBackgroundColor)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Шаг ${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyLarge
                )

                if (!step.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = rememberExplicitCacheImageRequest(step.imageUrl),
                        contentDescription = step.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        item {
            RecipeCommentsButton(
                count = comments.size,
                onClick = { showComments = true },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
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

    if (showIngredientsPicker) {
        IngredientsPickBottomSheet(
            ingredients = ingredientOptions,
            selected = selectedIngredients,
            onDismiss = { showIngredientsPicker = false },
            onAddClick = {
                onAddToShoppingList(
                    ingredientOptions
                        .filter { selectedIngredients[it.id] == true }
                        .map { it.title }
                )
                showIngredientsPicker = false
            }
        )
    }

    if (showAddMeal) {
        AddMealBottomSheet(
            mealType = MealType.BREAKFAST,
            onDismiss = { showAddMeal = false },
            onAddClick = { type, draft ->
                onAddMeal(type, draft)
                showAddMeal = false
            },
            allowMealTypeSelection = true,
            initialDraft = recipeDraft
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
        authorAvatarUrl = comment.authorAvatarUrl,
        parentId = comment.parentId,
        replyToName = comment.parentAuthorName,
        canDelete = currentUserId != null && currentUserId == comment.authorId
    )
}

@Composable
private fun RecipeTopBar(
    canEdit: Boolean,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onEatClick: () -> Unit
) {
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
            text = "Рецепт",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.weight(1f))
        if (canEdit) {
            Button(
                onClick = onEditClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF1E7DE),
                    contentColor = Color(0xFF4A4A4A)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = "Редактировать", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Button(
            onClick = onEatClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF1E7DE),
                contentColor = Color(0xFF4A4A4A)
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(text = "Я это съел", style = MaterialTheme.typography.bodyMedium)
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
}

@Composable
private fun AuthorHeader(post: PostCardState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AuthorAvatar(
            authorName = post.authorName,
            avatarUrl = post.authorAvatarUrl
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(post.authorName, fontWeight = FontWeight.SemiBold)
            Text(
                post.authorHandle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = post.date,
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
        val model = avatarUrl
            ?: "https://ui-avatars.com/api/?background=D8C2A0&color=FFFFFF&name=${authorName.replace(" ", "+")}"

        AsyncImage(
            model = rememberExplicitCacheImageRequest(model),
            contentDescription = "Аватар автора",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun IngredientsSection(
    post: PostCardState,
    onAddToListClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
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
                Button(
                    onClick = onAddToListClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF1E7DE),
                        contentColor = Color(0xFF4A4A4A)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "В список", style = MaterialTheme.typography.bodyMedium)
                }
            }

            post.ingredients.forEach { ingredient ->
                IngredientBulletItem(text = ingredient.text)
            }
        }
    }
}

@Composable
private fun IngredientBulletItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "\u2022",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 1.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}
