package ru.zagrebin.front_mobile.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.ui.components.postCard.ArticleCardContent
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardContent
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
import ru.zagrebin.front_mobile.ui.theme.AppPageBackgroundColor
import ru.zagrebin.front_mobile.ui.theme.SearchBarHorizontalPadding
import ru.zagrebin.front_mobile.ui.theme.SearchBarVerticalPadding
import ru.zagrebin.front_mobile.ui.theme.SearchFieldContainerColor
import ru.zagrebin.front_mobile.ui.theme.SearchFieldCornerRadius
import ru.zagrebin.front_mobile.ui.theme.ScrollTopButtonBottomPadding
import ru.zagrebin.front_mobile.ui.theme.ScrollTopButtonContainerColor
import ru.zagrebin.front_mobile.ui.theme.ScrollTopButtonContentColor
import ru.zagrebin.front_mobile.ui.theme.ScrollTopButtonEndPadding
import ru.zagrebin.front_mobile.ui.theme.TransparentColor
import ru.zagrebin.front_mobile.ui.theme.ListBottomPadding

private enum class MyPostsTab {
    Recipes,
    Articles,
    Saved
}

@Composable
fun MyPostsScreen(
    onBackClick: () -> Unit
) {
    val myPostsViewModel: MyPostsViewModel = viewModel()
    val state by myPostsViewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableStateOf(MyPostsTab.Recipes) }
    var isScrollingUp by remember { mutableStateOf(true) }
    var previousIndex by remember { mutableStateOf(0) }
    var previousOffset by remember { mutableStateOf(0) }

    val tabPosts = when (selectedTab) {
        MyPostsTab.Recipes -> state.recipes
        MyPostsTab.Articles -> state.articles
        MyPostsTab.Saved -> state.savedPosts
    }

    val filteredPosts = tabPosts

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collectLatest { (index, offset) ->
                isScrollingUp = if (index == previousIndex) {
                    offset < previousOffset
                } else {
                    index < previousIndex
                }
                previousIndex = index
                previousOffset = offset
            }
    }

    LaunchedEffect(listState, selectedTab, filteredPosts.size, state.isLoadingNextPage) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { firstVisibleIndex ->
                val hasMorePages = when (selectedTab) {
                    MyPostsTab.Recipes -> state.hasMoreRecipes
                    MyPostsTab.Articles -> state.hasMoreArticles
                    MyPostsTab.Saved -> state.hasMoreSavedPosts
                }
                val shouldLoadNextPage = hasMorePages &&
                    !state.isLoadingNextPage &&
                    filteredPosts.size >= 10 &&
                    firstVisibleIndex >= filteredPosts.size - 5
                if (shouldLoadNextPage) {
                    when (selectedTab) {
                        MyPostsTab.Recipes -> myPostsViewModel.loadNextRecipesPage()
                        MyPostsTab.Articles -> myPostsViewModel.loadNextArticlesPage()
                        MyPostsTab.Saved -> myPostsViewModel.loadNextSavedPostsPage()
                    }
                }
            }
    }

    val showTopControls = isScrollingUp ||
        (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0)
    val showScrollToTop = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 500
    val topOverlayHeight = 148.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppPageBackgroundColor)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = ListBottomPadding)
        ) {
            item {
                Spacer(modifier = Modifier.height(topOverlayHeight))
            }

            if (filteredPosts.isEmpty()) {
                item {
                    EmptyPostsState()
                }
            } else {
                items(filteredPosts, key = { "${it.type}-${it.id}" }) { post ->
                    MyPostCard(
                        post = post,
                        onTagClick = { tagId -> myPostsViewModel.onTagClick(post.id, tagId) },
                        onLikeClick = { myPostsViewModel.onLikeClick(post.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            MyPostsTopBar(onBackClick = onBackClick)

            AnimatedVisibility(
                visible = showTopControls,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = SearchBarHorizontalPadding,
                            vertical = SearchBarVerticalPadding
                        ),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SearchField(
                        value = state.searchQuery,
                        onValueChange = myPostsViewModel::onSearch
                    )

                    TabSelector(
                        selectedTab = selectedTab,
                        onTabChange = { selectedTab = it }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showScrollToTop,
            enter = fadeIn(),
            exit = fadeOut(),
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
    }
}

@Composable
private fun MyPostCard(
    post: PostCardState,
    onTagClick: (Int) -> Unit,
    onLikeClick: () -> Unit
) {
    if (post.type == "ARTICLE") {
        ArticleCardContent(
            state = post,
            onTagClick = onTagClick,
            onOpenArticle = {},
            onLikeClick = onLikeClick,
            onAuthorClick = {}
        )
    } else {
        PostCardContent(
            state = post,
            onTagClick = onTagClick,
            onOpenRecipe = {},
            onLikeClick = onLikeClick,
            onAuthorClick = {}
        )
    }
}

@Composable
private fun MyPostsTopBar(onBackClick: () -> Unit) {
    Surface(
        color = Color.White
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
                text = "Мои посты",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Поиск") },
        trailingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null
            )
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SearchFieldContainerColor,
            unfocusedContainerColor = SearchFieldContainerColor,
            focusedIndicatorColor = TransparentColor,
            unfocusedIndicatorColor = TransparentColor,
            disabledIndicatorColor = TransparentColor
        ),
        shape = RoundedCornerShape(SearchFieldCornerRadius),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun TabSelector(
    selectedTab: MyPostsTab,
    onTabChange: (MyPostsTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TabButton(
            title = "Рецепты",
            selected = selectedTab == MyPostsTab.Recipes,
            onClick = { onTabChange(MyPostsTab.Recipes) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            title = "Статьи",
            selected = selectedTab == MyPostsTab.Articles,
            onClick = { onTabChange(MyPostsTab.Articles) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            title = "Сохраненные",
            selected = selectedTab == MyPostsTab.Saved,
            onClick = { onTabChange(MyPostsTab.Saved) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) Color(0xFF7C3AED) else Color(0xFFF1E7DE),
        modifier = modifier
            .height(30.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                color = if (selected) Color.White else Color(0xFF4A4A4A),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun EmptyPostsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(Color(0xFFF1E7DE), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("0", style = MaterialTheme.typography.titleLarge, color = Color(0xFF8E8E8E))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Постов пока нет",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF8E8E8E)
        )
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun MyPostsScreenPreview() {
    MyPostsScreen(onBackClick = {})
}
