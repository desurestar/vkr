package ru.zagrebin.front_mobile.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.zagrebin.front_mobile.ui.screens.articles.ArticleDetailsScreen
import ru.zagrebin.front_mobile.ui.screens.articles.ArticlesFeedScreen
import ru.zagrebin.front_mobile.ui.screens.articles.CreateArticleScreen
import ru.zagrebin.front_mobile.ui.screens.entryOptions.EntryOptionsScreen
import ru.zagrebin.front_mobile.ui.screens.feed.FeedScreen
import ru.zagrebin.front_mobile.ui.screens.login.LoginScreen
import ru.zagrebin.front_mobile.ui.screens.profile.EditAccountScreen
import ru.zagrebin.front_mobile.ui.screens.profile.MyPostsScreen
import ru.zagrebin.front_mobile.ui.screens.profile.PasswordSecurityScreen
import ru.zagrebin.front_mobile.ui.screens.profile.ProfileScreen
import ru.zagrebin.front_mobile.ui.screens.profile.ProfileViewModel
import ru.zagrebin.front_mobile.ui.screens.profile.ShoppingListScreen
import ru.zagrebin.front_mobile.ui.screens.publicProfile.PublicProfileScreen
import ru.zagrebin.front_mobile.ui.screens.register.RegisterScreen
import ru.zagrebin.front_mobile.ui.screens.recipe.CreateRecipeScreen
import ru.zagrebin.front_mobile.ui.screens.recipe.RecipeDetailsScreen
import ru.zagrebin.front_mobile.ui.screens.statistics.StatisticsScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.data.AppContainer
import ru.zagrebin.front_mobile.data.remote.api.CreateRecipeIngredient
import ru.zagrebin.front_mobile.data.remote.api.CreateRecipeRequest
import ru.zagrebin.front_mobile.data.remote.api.CreateRecipeStep
import ru.zagrebin.front_mobile.data.repository.RefreshResult
import ru.zagrebin.front_mobile.ui.screens.profile.ProfileRepository
import ru.zagrebin.front_mobile.ui.screens.articles.ArticleDetailsViewModel
import ru.zagrebin.front_mobile.ui.screens.recipe.RecipeDetailsViewModel
import android.net.Uri
import java.io.File
import java.util.UUID

@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isAuthorized by AuthSessionState.isAuthorized.collectAsState()
    val appContainer = AppContainer(context)
    val api = appContainer.feedApi
    val profileRepository = ProfileRepository(appContainer.feedApi, appContainer.db.profileDao(), appContainer.networkConnectionChecker)
    val profileViewModel: ProfileViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Recipes.route,
        modifier = Modifier.padding(paddingValues)
    ) {

        composable(BottomNavItem.Recipes.route) {
            FeedScreen(
                onOpenRecipe = { postId ->
                    navController.navigate(Screen.RecipeDetails.createRoute(postId))
                },
                onOpenPublicProfile = { userId ->
                    navController.navigate(Screen.PublicProfile.createRoute(userId))
                }
            )
        }

        composable(BottomNavItem.Articles.route) {
            ArticlesFeedScreen(
                onOpenArticle = { postId ->
                    navController.navigate(Screen.ArticleDetails.createRoute(postId))
                },
                onOpenPublicProfile = { userId ->
                    navController.navigate(Screen.PublicProfile.createRoute(userId))
                }
            )
        }

        // Auth routes are also part of the same NavHost, otherwise navigate("register") crashes.
        composable(Screen.EntryOptions.route) {
            EntryOptionsScreen(navController)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController)
        }

        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }

        composable(BottomNavItem.Statistics.route) {
            StatisticsScreen()
        }

        composable(BottomNavItem.Profile.route) {
            LaunchedEffect(isAuthorized) {
                if (isAuthorized) {
                    profileViewModel.loadProfile()
                }
            }

            ProfileScreen(
                viewModel = profileViewModel,
                onOpenShoppingList = { navController.navigate(Screen.ShoppingList.route) },
                onOpenMyPosts = { navController.navigate(Screen.MyPosts.route) },
                onOpenEditAccount = { navController.navigate(Screen.EditAccount.route) },
                onOpenPasswordSecurity = { navController.navigate(Screen.PasswordSecurity.route) },
                onOpenCreateRecipe = { navController.navigate(Screen.CreateRecipe.route) },
                onOpenCreateArticle = { navController.navigate(Screen.CreateArticle.route) },
                onLogout = {
                    scope.launch {
                        runCatching { api.logout() }
                        profileRepository.clearProfile()
                        AuthSessionState.setAuthorized(context, false)
                        navController.navigate(Screen.EntryOptions.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.ShoppingList.route) {
            ShoppingListScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.MyPosts.route) {
            MyPostsScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.EditAccount.route) {
            val profileState = profileViewModel.state.collectAsState().value

            EditAccountScreen(
                initialName = profileState.name,
                initialAvatarUrl = profileState.avatarUrl,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { name, avatarUri ->
                    scope.launch {
                        val avatarUrl = avatarUri?.let { persistAvatarToAppStorage(context, it) }
                            ?: profileState.avatarUrl
                        runCatching { profileRepository.updateProfile(name, "", avatarUrl) }
                            .onSuccess {
                                profileViewModel.loadProfile()
                                navController.popBackStack()
                            }
                    }
                }
            )
        }

        composable(Screen.PasswordSecurity.route) {
            PasswordSecurityScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.CreateRecipe.route) {
            var availableTags by remember { mutableStateOf<List<String>>(emptyList()) }

            LaunchedEffect(Unit) {
                runCatching { appContainer.feedRepository.loadTagLabels() }
                    .onSuccess { result -> availableTags = result.data }
            }

            CreateRecipeScreen(
                onBackClick = { navController.popBackStack() },
                availableTags = availableTags,
                onPublish = { title, summary, content, cookTime, tags, ingredients, steps, recipePhotoUri, proteins, fats, carbs, kcal ->
                    scope.launch {
                        val publishResult = runCatching {
                            val mainImageUrl = persistRecipeImageToAppStorage(
                                context = context,
                                sourceUri = recipePhotoUri,
                                prefix = "recipe_main"
                            )
                            val stepImageUrls = steps.map { step ->
                                persistRecipeImageToAppStorage(
                                    context = context,
                                    sourceUri = step.photoUri,
                                    prefix = "recipe_step_${step.number}"
                                )
                            }

                            appContainer.feedRepository.createRecipe(
                                CreateRecipeRequest(
                                    title = title,
                                    summary = summary,
                                    content = content,
                                    imageUrl = mainImageUrl ?: stepImageUrls.firstOrNull { !it.isNullOrBlank() },
                                    cookTimeMinutes = cookTime,
                                    proteinsPer100 = proteins,
                                    fatsPer100 = fats,
                                    carbsPer100 = carbs,
                                    kcalPer100 = kcal,
                                    tags = tags,
                                    ingredients = ingredients.map { ingredient ->
                                        CreateRecipeIngredient(
                                            name = ingredient.name,
                                            amount = ingredient.amount.toDouble(),
                                            unit = ingredient.unit
                                        )
                                    },
                                    steps = steps.mapIndexed { index, step ->
                                        CreateRecipeStep(
                                            number = step.number,
                                            description = step.description,
                                            imageUrl = stepImageUrls[index]
                                        )
                                    }
                                )
                            )
                        }.getOrDefault(RefreshResult.Fallback)
                        if (publishResult == RefreshResult.Success) {
                            navController.popBackStack()
                        }
                    }
                }
            )
        }

        composable(Screen.CreateArticle.route) {
            CreateArticleScreen(onBackClick = { navController.popBackStack() })
        }

        composable(
            route = Screen.ArticleDetails.route,
            arguments = listOf(navArgument("postId") { type = NavType.IntType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getInt("postId") ?: return@composable
            val detailsViewModel: ArticleDetailsViewModel = viewModel()
            val state = detailsViewModel.state.collectAsState().value

            LaunchedEffect(postId) {
                detailsViewModel.load(postId)
            }

            when {
                state.post != null -> {
                    ArticleDetailsScreen(
                        article = state.post,
                        content = state.content,
                        onBackClick = { navController.popBackStack() }
                    )
                }
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Загрузка статьи...")
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Статья не найдена")
                    }
                }
            }
        }

        composable(
            route = Screen.RecipeDetails.route,
            arguments = listOf(navArgument("postId") { type = NavType.IntType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getInt("postId") ?: return@composable
            val detailsViewModel: RecipeDetailsViewModel = viewModel()
            val state = detailsViewModel.state.collectAsState().value

            LaunchedEffect(postId) {
                detailsViewModel.load(postId)
            }

            when {
                state.post != null -> {
                    RecipeDetailsScreen(
                        post = state.post,
                        onBackClick = { navController.popBackStack() }
                    )
                }
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Загрузка рецепта...")
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Рецепт не найден")
                    }
                }
            }
        }

        composable(
            route = Screen.PublicProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable

            PublicProfileScreen(
                userId = userId,
                onBackClick = { navController.popBackStack() },
                onOpenRecipe = { postId ->
                    navController.navigate(Screen.RecipeDetails.createRoute(postId))
                }
            )
        }
    }
}

private fun persistAvatarToAppStorage(context: android.content.Context, sourceUri: Uri): String? {
    return runCatching {
        val dir = File(context.filesDir, "avatars").apply { mkdirs() }
        val target = File(dir, "my_avatar.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        Uri.fromFile(target).toString()
    }.getOrNull()
}


private fun persistRecipeImageToAppStorage(context: android.content.Context, sourceUri: Uri?, prefix: String): String? {
    if (sourceUri == null) return null

    return runCatching {
        val dir = File(context.filesDir, "recipe_images").apply { mkdirs() }
        val target = File(dir, "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        Uri.fromFile(target).toString()
    }.getOrNull()
}
