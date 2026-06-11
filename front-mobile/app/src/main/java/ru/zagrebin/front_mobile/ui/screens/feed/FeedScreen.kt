package ru.zagrebin.front_mobile.ui.screens.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.ui.common.rememberExplicitCacheImageRequest
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardContent
import ru.zagrebin.front_mobile.ui.theme.FilterButtonCornerRadius
import ru.zagrebin.front_mobile.ui.theme.FilterButtonIconPadding
import ru.zagrebin.front_mobile.ui.theme.FilterButtonTonalElevation
import ru.zagrebin.front_mobile.ui.theme.ListBottomPadding
import ru.zagrebin.front_mobile.ui.theme.AppPageBackgroundColor
import ru.zagrebin.front_mobile.ui.theme.ScrollTopButtonBottomPadding
import ru.zagrebin.front_mobile.ui.theme.ScrollTopButtonContainerColor
import ru.zagrebin.front_mobile.ui.theme.ScrollTopButtonContentColor
import ru.zagrebin.front_mobile.ui.theme.ScrollTopButtonEndPadding
import ru.zagrebin.front_mobile.ui.theme.SearchBarHorizontalPadding
import ru.zagrebin.front_mobile.ui.theme.SearchBarItemsSpacing
import ru.zagrebin.front_mobile.ui.theme.SearchBarVerticalPadding
import ru.zagrebin.front_mobile.ui.theme.SearchFieldContainerColor
import ru.zagrebin.front_mobile.ui.theme.SearchFieldCornerRadius
import ru.zagrebin.front_mobile.ui.theme.TopBarHeight
import ru.zagrebin.front_mobile.ui.theme.TransparentColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel = viewModel(),
    isAuthorized: Boolean = true,
    onAuthRequired: () -> Unit = {},
    onOpenRecipe: (Int) -> Unit = {},
    onOpenPublicProfile: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var isFilterSheetOpen by rememberSaveable { mutableStateOf(false) }

    val topBarHeight = TopBarHeight
    val topContentPadding = topBarHeight

    val showScrollToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 500
        }
    }

    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = "Повторить"
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.retryRefresh()
        }
    }

    LaunchedEffect(listState, state.posts.size, state.hasMorePages, state.isLoadingNextPage) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { firstVisibleIndex ->
                val shouldLoadNextPage = state.hasMorePages &&
                    !state.isLoadingNextPage &&
                    state.posts.size >= 10 &&
                    firstVisibleIndex >= state.posts.size - 5
                if (shouldLoadNextPage) {
                    viewModel.loadNextPage()
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppPageBackgroundColor)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                top = topContentPadding,
                bottom = ListBottomPadding
            )
        ) {
            items(state.posts, key = { post -> post.id }) { post ->
                PostCardContent(
                    state = post,
                    onTagClick = { tagId ->
                        viewModel.onTagClick(post.id, tagId)
                    },
                    onOpenRecipe = onOpenRecipe,
                    onLikeClick = {
                        if (isAuthorized) viewModel.onLikeClick(post.id) else onAuthRequired()
                    },
                    onAuthorClick = onOpenPublicProfile
                )
            }

            if (state.posts.isEmpty() && state.filters.hasActiveFilters && !state.isUserSearch) {
                item {
                    FilterEmptyState(
                        title = "Ничего не найдено",
                        message = "Попробуйте изменить или сбросить фильтры."
                    )
                }
            }
        }

        SearchTopBar(
            query = state.searchQuery,
            onQueryChange = viewModel::onSearch,
            onFilterClick = { isFilterSheetOpen = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = SearchBarHorizontalPadding,
                    vertical = SearchBarVerticalPadding
                )
                .align(Alignment.TopCenter)
        )

        UserSearchResults(
            users = state.userResults,
            visible = state.isUserSearch,
            onOpenPublicProfile = { userId -> onOpenPublicProfile(userId.toString()) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SearchBarHorizontalPadding)
                .padding(top = topBarHeight + 8.dp)
                .align(Alignment.TopCenter)
        )

        AnimatedVisibility(
            visible = showScrollToTop,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = ScrollTopButtonEndPadding,
                    bottom = ScrollTopButtonBottomPadding
                )
        ) {
            FloatingActionButton(
                onClick = { scope.launch { listState.animateScrollToItem(0) } },
                containerColor = ScrollTopButtonContainerColor,
                contentColor = ScrollTopButtonContentColor
            ) {
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowUp,
                    contentDescription = "Наверх"
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = ListBottomPadding)
        )
    }

    if (isFilterSheetOpen) {
        FeedFiltersSheet(
            title = "Фильтры рецептов",
            filters = state.filters,
            tagQuery = state.tagQuery,
            tagSuggestions = state.tagSuggestions,
            showRecipeRanges = true,
            onFilterChange = viewModel::onFilterDraftChange,
            onTagQueryChange = viewModel::onTagQueryChange,
            onAddTag = viewModel::onFilterTagAdd,
            onRemoveTag = viewModel::onFilterTagRemove,
            onApply = {
                viewModel.onFilterApply()
                isFilterSheetOpen = false
            },
            onClear = viewModel::onFilterClear,
            onDismiss = { isFilterSheetOpen = false }
        )
    }

}

@Composable
fun UserSearchResults(
    users: List<UserSearchState>,
    visible: Boolean,
    onOpenPublicProfile: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = visible && users.isNotEmpty(), modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SearchFieldContainerColor,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Пользователи",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
                users.forEach { user ->
                    UserSearchResultRow(
                        user = user,
                        onClick = { onOpenPublicProfile(user.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UserSearchResultRow(
    user: UserSearchState,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFD8C2A0)),
            contentAlignment = Alignment.Center
        ) {
            val avatarUrl = user.avatarUrl?.takeIf { it.isNotBlank() }
            if (avatarUrl == null) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                AsyncImage(
                    model = rememberExplicitCacheImageRequest(avatarUrl),
                    contentDescription = "Аватар пользователя",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = user.displayName.ifBlank { "Пользователь" },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = user.username.withAtPrefix(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FilterEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        shape = RoundedCornerShape(24.dp),
        color = SearchFieldContainerColor,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

private fun String.withAtPrefix(): String = when {
    isBlank() -> "@user"
    startsWith("@") -> this
    else -> "@$this"
}

@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SearchBarItemsSpacing)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Поиск") },
            singleLine = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Искать"
                )
            },
            shape = RoundedCornerShape(SearchFieldCornerRadius),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SearchFieldContainerColor,
                unfocusedContainerColor = SearchFieldContainerColor,
                focusedIndicatorColor = TransparentColor,
                unfocusedIndicatorColor = TransparentColor,
                disabledIndicatorColor = TransparentColor
            )
        )

        Surface(
            onClick = onFilterClick,
            shape = RoundedCornerShape(FilterButtonCornerRadius),
            color = SearchFieldContainerColor,
            tonalElevation = FilterButtonTonalElevation
        ) {
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = "Фильтры",
                modifier = Modifier.padding(FilterButtonIconPadding)
            )
        }
    }
}
