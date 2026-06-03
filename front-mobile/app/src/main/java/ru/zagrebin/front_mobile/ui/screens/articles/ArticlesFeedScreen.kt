package ru.zagrebin.front_mobile.ui.screens.articles

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.ui.components.postCard.ArticleCardContent
import ru.zagrebin.front_mobile.ui.screens.feed.UserSearchState
import ru.zagrebin.front_mobile.ui.theme.AppPageBackgroundColor
import ru.zagrebin.front_mobile.ui.theme.FilterButtonCornerRadius
import ru.zagrebin.front_mobile.ui.theme.FilterButtonIconPadding
import ru.zagrebin.front_mobile.ui.theme.FilterButtonTonalElevation
import ru.zagrebin.front_mobile.ui.theme.FilterFieldContainerColor
import ru.zagrebin.front_mobile.ui.theme.FilterFieldCornerRadius
import ru.zagrebin.front_mobile.ui.theme.FilterRangeFieldsSpacing
import ru.zagrebin.front_mobile.ui.theme.FilterSheetBottomSpacer
import ru.zagrebin.front_mobile.ui.theme.FilterSheetContentSpacing
import ru.zagrebin.front_mobile.ui.theme.FilterSheetCornerRadius
import ru.zagrebin.front_mobile.ui.theme.FilterSheetHorizontalPadding
import ru.zagrebin.front_mobile.ui.theme.FilterSheetVerticalPadding
import ru.zagrebin.front_mobile.ui.theme.ListBottomPadding
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesFeedScreen(
    viewModel: ArticlesViewModel = viewModel(),
    onOpenArticle: (Int) -> Unit = {},
    onOpenPublicProfile: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var isFilterSheetOpen by rememberSaveable { mutableStateOf(false) }
    var minTime by rememberSaveable { mutableStateOf("") }
    var maxTime by rememberSaveable { mutableStateOf("") }
    var minCalories by rememberSaveable { mutableStateOf("") }
    var maxCalories by rememberSaveable { mutableStateOf("") }

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
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                top = topContentPadding,
                bottom = ListBottomPadding
            )
        ) {
            items(state.posts, key = { post -> post.id }) { post ->
                ArticleCardContent(
                    state = post,
                    onTagClick = { tagId ->
                        viewModel.onTagClick(post.id, tagId)
                    },
                    onOpenArticle = onOpenArticle,
                    onLikeClick = { viewModel.onLikeClick(post.id) },
                    onAuthorClick = onOpenPublicProfile
                )
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
        ModalBottomSheet(
            onDismissRequest = { isFilterSheetOpen = false },
            containerColor = AppPageBackgroundColor,
            contentColor = MaterialTheme.colorScheme.onBackground,
            shape = RoundedCornerShape(
                topStart = FilterSheetCornerRadius,
                topEnd = FilterSheetCornerRadius
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = FilterSheetHorizontalPadding,
                        vertical = FilterSheetVerticalPadding
                    )
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(FilterSheetContentSpacing)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Фильтры", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { isFilterSheetOpen = false }) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "Закрыть")
                    }
                }

                Text(text = "Время готовки, мин", style = MaterialTheme.typography.titleMedium)
                FilterRangeRow(
                    fromValue = minTime,
                    toValue = maxTime,
                    onFromChange = { minTime = it },
                    onToChange = { maxTime = it }
                )

                Text(text = "Калории", style = MaterialTheme.typography.titleMedium)
                FilterRangeRow(
                    fromValue = minCalories,
                    toValue = maxCalories,
                    onFromChange = { minCalories = it },
                    onToChange = { maxCalories = it }
                )

                Spacer(modifier = Modifier.height(FilterSheetBottomSpacer))
            }
        }
    }
}

@Composable
private fun UserSearchResults(
    users: List<UserSearchState>,
    visible: Boolean,
    onOpenPublicProfile: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = visible && users.isNotEmpty(), modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = SearchFieldContainerColor,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                users.forEach { user ->
                    Surface(
                        onClick = { onOpenPublicProfile(user.id) },
                        color = TransparentColor,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = user.displayName.ifBlank { "Пользователь" },
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = if (user.username.startsWith("@")) user.username else "@${user.username}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
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

@Composable
private fun FilterRangeRow(
    fromValue: String,
    toValue: String,
    onFromChange: (String) -> Unit,
    onToChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FilterRangeFieldsSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = fromValue,
            onValueChange = onFromChange,
            placeholder = { Text("От") },
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(FilterFieldCornerRadius),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FilterFieldContainerColor,
                unfocusedContainerColor = FilterFieldContainerColor,
                focusedIndicatorColor = TransparentColor,
                unfocusedIndicatorColor = TransparentColor,
                disabledIndicatorColor = TransparentColor
            )
        )

        Text(text = "—")

        TextField(
            value = toValue,
            onValueChange = onToChange,
            placeholder = { Text("До") },
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(FilterFieldCornerRadius),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FilterFieldContainerColor,
                unfocusedContainerColor = FilterFieldContainerColor,
                focusedIndicatorColor = TransparentColor,
                unfocusedIndicatorColor = TransparentColor,
                disabledIndicatorColor = TransparentColor
            )
        )
    }
}
