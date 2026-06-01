package ru.zagrebin.front_mobile.ui.navigation

import android.content.Context
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
import ru.zagrebin.front_mobile.data.remote.api.CreateArticleRequest
import ru.zagrebin.front_mobile.data.remote.api.CreateRecipeIngredient
import ru.zagrebin.front_mobile.data.remote.api.CreateRecipeRequest
import ru.zagrebin.front_mobile.data.remote.api.CreateRecipeStep
import ru.zagrebin.front_mobile.data.remote.api.FeedApi
import ru.zagrebin.front_mobile.data.repository.CreateArticleResult
import ru.zagrebin.front_mobile.data.repository.CreateRecipeResult
import ru.zagrebin.front_mobile.ui.screens.profile.ProfileRepository
import ru.zagrebin.front_mobile.ui.screens.articles.ArticleDetailsViewModel
import ru.zagrebin.front_mobile.ui.screens.recipe.RecipeDetailsViewModel
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
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
                        val avatarUrl = avatarUri?.let {
                            uploadAvatarToServer(context, appContainer.feedApi, it)
                        } ?: profileState.avatarUrl
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
                            val mainImageUrl = uploadRecipeImageToServer(
                                context = context,
                                api = appContainer.feedApi,
                                sourceUri = recipePhotoUri,
                                prefix = "recipe_main"
                            )
                            val stepImageUrls = steps.map { step ->
                                uploadRecipeImageToServer(
                                    context = context,
                                    api = appContainer.feedApi,
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
                        }.getOrDefault(CreateRecipeResult.Fallback)
                        if (publishResult is CreateRecipeResult.Success) {
                            navController.navigate(Screen.RecipeDetails.createRoute(publishResult.postId)) {
                                popUpTo(Screen.CreateRecipe.route) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        composable(Screen.CreateArticle.route) {
            var availableTags by remember { mutableStateOf<List<String>>(emptyList()) }

            LaunchedEffect(Unit) {
                runCatching { appContainer.feedRepository.loadTagLabels() }
                    .onSuccess { result -> availableTags = result.data }
            }

            CreateArticleScreen(
                onBackClick = { navController.popBackStack() },
                availableTags = availableTags,
                onPublish = { title, summary, content, tags, coverUri, blocks ->
                    scope.launch {
                        val publishResult = runCatching {
                            val coverImageUrl = uploadRecipeImageToServer(
                                context = context,
                                api = appContainer.feedApi,
                                sourceUri = coverUri,
                                prefix = "article_cover"
                            )
                            val blockImageUrls = blocks.map { block ->
                                uploadRecipeImageToServer(
                                    context = context,
                                    api = appContainer.feedApi,
                                    sourceUri = block.photoUri,
                                    prefix = "article_block_${block.number}"
                                )
                            }
                            val contentWithImages = blocks.mapIndexed { index, block ->
                                buildString {
                                    append("## ").append(block.title).append("\n")
                                    append(block.content)
                                    val imageUrl = blockImageUrls[index]
                                    if (!imageUrl.isNullOrBlank()) {
                                        append("\n[image:").append(imageUrl).append(']')
                                    }
                                }
                            }.joinToString("\n\n").ifBlank { content }

                            appContainer.feedRepository.createArticle(
                                CreateArticleRequest(
                                    title = title,
                                    summary = summary,
                                    content = contentWithImages,
                                    imageUrl = coverImageUrl ?: blockImageUrls.firstOrNull { !it.isNullOrBlank() },
                                    tags = tags
                                )
                            )
                        }.getOrDefault(CreateArticleResult.Fallback)
                        if (publishResult is CreateArticleResult.Success) {
                            navController.navigate(Screen.ArticleDetails.createRoute(publishResult.postId)) {
                                popUpTo(Screen.CreateArticle.route) { inclusive = true }
                            }
                        }
                    }
                }
            )
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
                        currentUserId = state.currentUserId,
                        onBackClick = { navController.popBackStack() },
                        onSendComment = detailsViewModel::addComment,
                        onDeleteComment = detailsViewModel::deleteComment
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
                        currentUserId = state.currentUserId,
                        onBackClick = { navController.popBackStack() },
                        onSendComment = detailsViewModel::addComment,
                        onDeleteComment = detailsViewModel::deleteComment
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


private suspend fun uploadRecipeImageToServer(context: android.content.Context, api: FeedApi, sourceUri: Uri?, prefix: String): String? {
    if (sourceUri == null) return null

    return runCatching {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(sourceUri) ?: "image/jpeg"
        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val target = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID()}.$extension")
        try {
            contentResolver.openInputStream(sourceUri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return null

            val requestBody = target.asRequestBody(mimeType.toMediaTypeOrNull())
            val multipart = MultipartBody.Part.createFormData("file", target.name, requestBody)
            api.uploadMedia(multipart).url
        } finally {
            target.delete()
        }
    }.getOrNull()
}

private suspend fun uploadAvatarToServer(
    context: Context,
    api: FeedApi,
    sourceUri: Uri
): String? {
    return runCatching {
        val mimeType = context.contentResolver.getType(sourceUri) ?: "image/jpeg"

        val file = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return null

        val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
        val multipart = MultipartBody.Part.createFormData("file", file.name, requestBody)

        api.uploadMedia(multipart).url
    }.getOrNull()
}
