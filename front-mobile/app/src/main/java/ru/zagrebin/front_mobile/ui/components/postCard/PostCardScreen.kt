package ru.zagrebin.front_mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.tooling.preview.Preview
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardContent
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardViewModel
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagScreen
import ru.zagrebin.front_mobile.ui.components.recipeTag.TagState

@Composable
fun PostCardScreen(
    viewModel: PostCardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    PostCardContent(
        state = state,
        onTagClick = viewModel::onTagClick,
        onOpenRecipe = {}
    )
}