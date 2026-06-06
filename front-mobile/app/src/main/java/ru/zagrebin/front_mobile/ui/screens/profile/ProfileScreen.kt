package ru.zagrebin.front_mobile.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.BookOpen
import compose.icons.feathericons.Bookmark
import compose.icons.feathericons.Folder
import compose.icons.feathericons.Lock
import compose.icons.feathericons.LogOut
import compose.icons.feathericons.User
import ru.zagrebin.front_mobile.ui.components.BottomBarIslandDefaults
import ru.zagrebin.front_mobile.ui.screens.profile.components.ProfileEvent
import ru.zagrebin.front_mobile.ui.screens.profile.components.ProfileHeader
import ru.zagrebin.front_mobile.ui.screens.profile.components.ProfileMenuGroup
import ru.zagrebin.front_mobile.ui.screens.profile.components.ProfileMenuItem
import ru.zagrebin.front_mobile.ui.screens.profile.components.ProfileSectionTitle
import ru.zagrebin.front_mobile.ui.screens.profile.components.ProfileStatsCard

private val ProfileBottomMenuPadding = BottomBarIslandDefaults.BottomPadding + 54.dp

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    isAuthorized: Boolean = true,
    onOpenShoppingList: () -> Unit,
    onOpenMyPosts: () -> Unit,
    onOpenDrafts: () -> Unit,
    onOpenEditAccount: () -> Unit,
    onOpenPasswordSecurity: () -> Unit,
    onOpenCreateRecipe: () -> Unit,
    onOpenCreateArticle: () -> Unit,
    onLogout: () -> Unit
) {
    val state = viewModel.state.collectAsState().value

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator()
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F3F3))
    ) {

        LazyColumn(
            contentPadding = PaddingValues(
                bottom = ProfileBottomMenuPadding
            )
        ) {

            item {
                ProfileHeader(
                    name = if (isAuthorized) state.name else "Гость",
                    email = if (isAuthorized) state.email else "Войдите, чтобы публиковать и сохранять данные",
                    avatarUrl = if (isAuthorized) state.avatarUrl else null,
                    onCreateRecipeClick = {
                        viewModel.onEvent(ProfileEvent.OnCreateRecipeClick)
                        onOpenCreateRecipe()
                    },
                    onCreateArticleClick = {
                        viewModel.onEvent(ProfileEvent.OnCreateArticleClick)
                        onOpenCreateArticle()
                    }
                )
            }

            item { Spacer(Modifier.height(20.dp)) }

            item {
                ProfileStatsCard(
                    following = if (isAuthorized) state.following else "0",
                    followers = if (isAuthorized) state.followers else "0",
                    likes = if (isAuthorized) state.likes else "0"
                )
            }

            item { Spacer(Modifier.height(6.dp)) }

            item { ProfileSectionTitle("Настройки профиля") }

            item {
                ProfileMenuGroup {

                    ProfileMenuItem(
                        icon = FeatherIcons.BookOpen,
                        title = "Мои посты",
                        onClick = {
                            viewModel.onEvent(ProfileEvent.OnMyPostsClick)
                            onOpenMyPosts()
                        }
                    )

                    ProfileMenuItem(
                        icon = FeatherIcons.Bookmark,
                        title = "Список покупок",
                        onClick = {
                            viewModel.onEvent(ProfileEvent.OnShoppingListClick)
                            onOpenShoppingList()
                        }
                    )

                    ProfileMenuItem(
                        icon = FeatherIcons.Folder,
                        title = "Черновики",
                        onClick = {
                            viewModel.onEvent(ProfileEvent.OnDraftsClick)
                            onOpenDrafts()
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(6.dp)) }

            item { ProfileSectionTitle("Аккаунт") }

            item {
                ProfileMenuGroup {

                    ProfileMenuItem(
                        icon = FeatherIcons.User,
                        title = "Редактировать аккаунт",
                        onClick = {
                            viewModel.onEvent(ProfileEvent.OnEditAccountClick)
                            onOpenEditAccount()
                        }
                    )

                    ProfileMenuItem(
                        icon = FeatherIcons.Lock,
                        title = "Пароль и безопасность",
                        onClick = {
                            viewModel.onEvent(ProfileEvent.OnSecurityClick)
                            onOpenPasswordSecurity()
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(40.dp)) }

            item {
                ProfileMenuGroup {
                    ProfileMenuItem(
                        icon = FeatherIcons.LogOut,
                        title = if (isAuthorized) "Выйти из аккаунта" else "Войти или зарегистрироваться",
                        onClick = onLogout
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        isAuthorized = false,
        onOpenShoppingList = {},
        onOpenMyPosts = {},
        onOpenDrafts = {},
        onOpenEditAccount = {},
        onOpenPasswordSecurity = {},
        onOpenCreateRecipe = {},
        onOpenCreateArticle = {},
        onLogout = {}
    )
}
