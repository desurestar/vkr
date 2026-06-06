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
import ru.zagrebin.front_mobile.ui.screens.articles.ArticleBlockDraft
import ru.zagrebin.front_mobile.ui.screens.articles.ArticleEditDraft
import ru.zagrebin.front_mobile.ui.screens.articles.CreateArticleScreen
import ru.zagrebin.front_mobile.ui.screens.entryOptions.EntryOptionsScreen
import ru.zagrebin.front_mobile.ui.screens.feed.FeedScreen
import ru.zagrebin.front_mobile.ui.screens.login.LoginScreen
import ru.zagrebin.front_mobile.ui.screens.profile.DraftsScreen
import ru.zagrebin.front_mobile.ui.screens.profile.EditAccountScreen
import ru.zagrebin.front_mobile.ui.screens.profile.MyPostsScreen
import ru.zagrebin.front_mobile.ui.screens.profile.PasswordSecurityScreen
import ru.zagrebin.front_mobile.ui.screens.profile.ProfileScreen
import ru.zagrebin.front_mobile.ui.screens.profile.ProfileViewModel
import ru.zagrebin.front_mobile.ui.screens.profile.ShoppingListScreen
import ru.zagrebin.front_mobile.ui.screens.publicProfile.PublicProfileScreen
import ru.zagrebin.front_mobile.ui.screens.register.RegisterScreen
import ru.zagrebin.front_mobile.ui.screens.recipe.CreateRecipeScreen
import ru.zagrebin.front_mobile.ui.screens.recipe.IngredientDraft
import ru.zagrebin.front_mobile.ui.screens.recipe.RecipeEditDraft
import ru.zagrebin.front_mobile.ui.screens.recipe.RecipeStepDraft
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
import ru.zagrebin.front_mobile.ui.components.postCard.PostCardState
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
    val requestAuthorization = { navController.navigate(Screen.EntryOptions.route) { launchSingleTop = true } }
    val requireAuthorization: (() -> Unit) -> Unit = { action ->
        if (isAuthorized) action() else requestAuthorization()
    }

    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Recipes.route,
        modifier = Modifier.padding(paddingValues)
    ) {

        composable(BottomNavItem.Recipes.route) {
            FeedScreen(
                isAuthorized = isAuthorized,
                onAuthRequired = requestAuthorization,
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
                isAuthorized = isAuthorized,
                onAuthRequired = requestAuthorization,
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
                isAuthorized = isAuthorized,
                onOpenShoppingList = { navController.navigate(Screen.ShoppingList.route) },
                onOpenMyPosts = { requireAuthorization { navController.navigate(Screen.MyPosts.route) } },
                onOpenDrafts = { navController.navigate(Screen.Drafts.route) },
                onOpenEditAccount = { requireAuthorization { navController.navigate(Screen.EditAccount.route) } },
                onOpenPasswordSecurity = { requireAuthorization { navController.navigate(Screen.PasswordSecurity.route) } },
                onOpenCreateRecipe = { navController.navigate(Screen.CreateRecipe.route) },
                onOpenCreateArticle = { navController.navigate(Screen.CreateArticle.route) },
                onLogout = {
                    if (!isAuthorized) {
                        requestAuthorization()
                    } else {
                        scope.launch {
                            runCatching { api.logout() }
                            appContainer.clearAuthorizedUserData()
                            AuthSessionState.setAuthorized(context, false)
                            navController.navigate(Screen.EntryOptions.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        composable(Screen.ShoppingList.route) {
            ShoppingListScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.MyPosts.route) {
            if (!isAuthorized) {
                LaunchedEffect(Unit) { requestAuthorization() }
                return@composable
            }
            MyPostsScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.Drafts.route) {
            DraftsScreen(
                onBackClick = { navController.popBackStack() },
                onOpenRecipe = { postId -> navController.navigate(Screen.RecipeDetails.createRoute(postId)) },
                onOpenArticle = { postId -> navController.navigate(Screen.ArticleDetails.createRoute(postId)) }
            )
        }

        composable(Screen.EditAccount.route) {
            if (!isAuthorized) {
                LaunchedEffect(Unit) { requestAuthorization() }
                return@composable
            }
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
            if (!isAuthorized) {
                LaunchedEffect(Unit) { requestAuthorization() }
                return@composable
            }
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
                onPublish = { title, summary, content, cookTime, tags, ingredients, steps, recipePhotoUri, existingRecipeImageUrl, proteins, fats, carbs, kcal ->
                    if (!isAuthorized) {
                        requestAuthorization()
                    } else {
                    scope.launch {
                        val publishResult = runCatching {
                            val mainImageUrl = uploadRecipeImageToServer(
                                context = context,
                                api = appContainer.feedApi,
                                sourceUri = recipePhotoUri,
                                prefix = "recipe_main"
                            ) ?: existingRecipeImageUrl
                            val stepImageUrls = steps.map { step ->
                                uploadRecipeImageToServer(
                                    context = context,
                                    api = appContainer.feedApi,
                                    sourceUri = step.photoUri,
                                    prefix = "recipe_step_${step.number}"
                                ) ?: step.existingImageUrl
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
                                    status = "PUBLISHED",
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
                },
                onDraft = { title, summary, content, cookTime, tags, ingredients, steps, recipePhotoUri, existingRecipeImageUrl, proteins, fats, carbs, kcal ->
                    scope.launch {
                        val draftResult = runCatching {
                            val canUploadDraftImages = isAuthorized && appContainer.networkConnectionChecker.isNetworkAvailable()
                            val mainImageUrl = if (canUploadDraftImages) {
                                uploadRecipeImageToServer(context, appContainer.feedApi, recipePhotoUri, "recipe_draft_main") ?: existingRecipeImageUrl
                            } else {
                                recipePhotoUri?.toString() ?: existingRecipeImageUrl
                            }
                            val stepImageUrls = steps.map { step ->
                                if (canUploadDraftImages) {
                                    uploadRecipeImageToServer(context, appContainer.feedApi, step.photoUri, "recipe_draft_step_${step.number}") ?: step.existingImageUrl
                                } else {
                                    step.photoUri?.toString() ?: step.existingImageUrl
                                }
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
                                    status = "DRAFT",
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
                        if (draftResult is CreateRecipeResult.Success) {
                            navController.navigate(Screen.Drafts.route) {
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
                onPublish = { title, summary, content, tags, coverUri, existingCoverUrl, blocks ->
                    if (!isAuthorized) {
                        requestAuthorization()
                    } else {
                    scope.launch {
                        val publishResult = runCatching {
                            val coverImageUrl = uploadRecipeImageToServer(
                                context = context,
                                api = appContainer.feedApi,
                                sourceUri = coverUri,
                                prefix = "article_cover"
                            ) ?: existingCoverUrl
                            val blockImageUrls = blocks.map { block ->
                                uploadRecipeImageToServer(
                                    context = context,
                                    api = appContainer.feedApi,
                                    sourceUri = block.photoUri,
                                    prefix = "article_block_${block.number}"
                                ) ?: block.existingImageUrl
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
                                    status = "PUBLISHED",
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
                },
                onDraft = { title, summary, content, tags, coverUri, existingCoverUrl, blocks ->
                    scope.launch {
                        val draftResult = runCatching {
                            val canUploadDraftImages = isAuthorized && appContainer.networkConnectionChecker.isNetworkAvailable()
                            val coverImageUrl = if (canUploadDraftImages) {
                                uploadRecipeImageToServer(context, appContainer.feedApi, coverUri, "article_draft_cover") ?: existingCoverUrl
                            } else {
                                coverUri?.toString() ?: existingCoverUrl
                            }
                            val blockImageUrls = blocks.map { block ->
                                if (canUploadDraftImages) {
                                    uploadRecipeImageToServer(context, appContainer.feedApi, block.photoUri, "article_draft_block_${block.number}") ?: block.existingImageUrl
                                } else {
                                    block.photoUri?.toString() ?: block.existingImageUrl
                                }
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
                                    status = "DRAFT",
                                    tags = tags
                                )
                            )
                        }.getOrDefault(CreateArticleResult.Fallback)
                        if (draftResult is CreateArticleResult.Success) {
                            navController.navigate(Screen.Drafts.route) {
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
                        isAuthorized = isAuthorized,
                        onAuthRequired = requestAuthorization,
                        onBackClick = { navController.popBackStack() },
                        onEditClick = { navController.navigate(Screen.EditArticle.createRoute(postId)) },
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
                        isAuthorized = isAuthorized,
                        onAuthRequired = requestAuthorization,
                        onBackClick = { navController.popBackStack() },
                        onEditClick = { navController.navigate(Screen.EditRecipe.createRoute(postId)) },
                        onSendComment = detailsViewModel::addComment,
                        onDeleteComment = detailsViewModel::deleteComment,
                        onAddToShoppingList = detailsViewModel::addIngredientsToShoppingList,
                        onAddMeal = detailsViewModel::addMeal
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
            route = Screen.EditRecipe.route,
            arguments = listOf(navArgument("postId") { type = NavType.IntType })
        ) { backStackEntry ->
            if (!isAuthorized) {
                LaunchedEffect(Unit) { requestAuthorization() }
                return@composable
            }
            val postId = backStackEntry.arguments?.getInt("postId") ?: return@composable
            val detailsViewModel: RecipeDetailsViewModel = viewModel()
            val state = detailsViewModel.state.collectAsState().value
            var availableTags by remember { mutableStateOf<List<String>>(emptyList()) }

            LaunchedEffect(postId) { detailsViewModel.load(postId) }
            LaunchedEffect(Unit) {
                runCatching { appContainer.feedRepository.loadTagLabels() }
                    .onSuccess { result -> availableTags = result.data }
            }

            val post = state.post
            when {
                post != null && state.currentUserId != null && post.authorId == state.currentUserId.toString() -> {
                    CreateRecipeScreen(
                        onBackClick = { navController.popBackStack() },
                        availableTags = availableTags,
                        initialDraft = post.toRecipeEditDraft(),
                        isEditMode = true,
                        onPublish = { title, summary, content, cookTime, tags, ingredients, steps, recipePhotoUri, existingRecipeImageUrl, proteins, fats, carbs, kcal ->
                            scope.launch {
                                val saveResult = runCatching {
                                    val mainImageUrl = uploadRecipeImageToServer(context, appContainer.feedApi, recipePhotoUri, "recipe_edit_main") ?: existingRecipeImageUrl
                                    val stepImageUrls = steps.map { step ->
                                        uploadRecipeImageToServer(context, appContainer.feedApi, step.photoUri, "recipe_edit_step_${step.number}") ?: step.existingImageUrl
                                    }
                                    appContainer.feedRepository.updateRecipe(
                                        postId,
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
                                            status = "PUBLISHED",
                                            tags = tags,
                                            ingredients = ingredients.map { CreateRecipeIngredient(it.name, it.amount.toDouble(), it.unit) },
                                            steps = steps.mapIndexed { index, step -> CreateRecipeStep(step.number, step.description, stepImageUrls[index]) }
                                        )
                                    )
                                }.getOrDefault(CreateRecipeResult.Fallback)
                                if (saveResult is CreateRecipeResult.Success) {
                                    navController.navigate(Screen.RecipeDetails.createRoute(saveResult.postId)) {
                                        popUpTo(Screen.EditRecipe.route) { inclusive = true }
                                    }
                                }
                            }
                        },
                        onDraft = { title, summary, content, cookTime, tags, ingredients, steps, recipePhotoUri, existingRecipeImageUrl, proteins, fats, carbs, kcal ->
                            scope.launch {
                                val saveResult = runCatching {
                                    val mainImageUrl = uploadRecipeImageToServer(context, appContainer.feedApi, recipePhotoUri, "recipe_edit_draft_main") ?: existingRecipeImageUrl
                                    val stepImageUrls = steps.map { step ->
                                        uploadRecipeImageToServer(context, appContainer.feedApi, step.photoUri, "recipe_edit_draft_step_${step.number}") ?: step.existingImageUrl
                                    }
                                    appContainer.feedRepository.updateRecipe(
                                        postId,
                                        CreateRecipeRequest(title, summary, content, mainImageUrl ?: stepImageUrls.firstOrNull { !it.isNullOrBlank() }, cookTime, proteins, fats, carbs, kcal, "DRAFT", tags, ingredients.map { CreateRecipeIngredient(it.name, it.amount.toDouble(), it.unit) }, steps.mapIndexed { index, step -> CreateRecipeStep(step.number, step.description, stepImageUrls[index]) })
                                    )
                                }.getOrDefault(CreateRecipeResult.Fallback)
                                if (saveResult is CreateRecipeResult.Success) {
                                    navController.navigate(Screen.Drafts.route) { popUpTo(Screen.EditRecipe.route) { inclusive = true } }
                                }
                            }
                        }
                    )
                }
                state.isLoading -> EditLoadingText("Загрузка рецепта...")
                else -> EditLoadingText("Редактирование доступно только автору рецепта")
            }
        }

        composable(
            route = Screen.EditArticle.route,
            arguments = listOf(navArgument("postId") { type = NavType.IntType })
        ) { backStackEntry ->
            if (!isAuthorized) {
                LaunchedEffect(Unit) { requestAuthorization() }
                return@composable
            }
            val postId = backStackEntry.arguments?.getInt("postId") ?: return@composable
            val detailsViewModel: ArticleDetailsViewModel = viewModel()
            val state = detailsViewModel.state.collectAsState().value
            var availableTags by remember { mutableStateOf<List<String>>(emptyList()) }

            LaunchedEffect(postId) { detailsViewModel.load(postId) }
            LaunchedEffect(Unit) {
                runCatching { appContainer.feedRepository.loadTagLabels() }
                    .onSuccess { result -> availableTags = result.data }
            }

            val post = state.post
            when {
                post != null && state.currentUserId != null && post.authorId == state.currentUserId.toString() -> {
                    CreateArticleScreen(
                        onBackClick = { navController.popBackStack() },
                        availableTags = availableTags,
                        initialDraft = post.toArticleEditDraft(state.content),
                        isEditMode = true,
                        onPublish = { title, summary, content, tags, coverUri, existingCoverUrl, blocks ->
                            scope.launch {
                                val saveResult = runCatching {
                                    val coverImageUrl = uploadRecipeImageToServer(context, appContainer.feedApi, coverUri, "article_edit_cover") ?: existingCoverUrl
                                    val blockImageUrls = blocks.map { block ->
                                        uploadRecipeImageToServer(context, appContainer.feedApi, block.photoUri, "article_edit_block_${block.number}") ?: block.existingImageUrl
                                    }
                                    val contentWithImages = blocks.toContentWithImages(blockImageUrls, content)
                                    appContainer.feedRepository.updateArticle(
                                        postId,
                                        CreateArticleRequest(title, summary, contentWithImages, coverImageUrl ?: blockImageUrls.firstOrNull { !it.isNullOrBlank() }, "PUBLISHED", tags)
                                    )
                                }.getOrDefault(CreateArticleResult.Fallback)
                                if (saveResult is CreateArticleResult.Success) {
                                    navController.navigate(Screen.ArticleDetails.createRoute(saveResult.postId)) { popUpTo(Screen.EditArticle.route) { inclusive = true } }
                                }
                            }
                        },
                        onDraft = { title, summary, content, tags, coverUri, existingCoverUrl, blocks ->
                            scope.launch {
                                val saveResult = runCatching {
                                    val coverImageUrl = uploadRecipeImageToServer(context, appContainer.feedApi, coverUri, "article_edit_draft_cover") ?: existingCoverUrl
                                    val blockImageUrls = blocks.map { block ->
                                        uploadRecipeImageToServer(context, appContainer.feedApi, block.photoUri, "article_edit_draft_block_${block.number}") ?: block.existingImageUrl
                                    }
                                    val contentWithImages = blocks.toContentWithImages(blockImageUrls, content)
                                    appContainer.feedRepository.updateArticle(postId, CreateArticleRequest(title, summary, contentWithImages, coverImageUrl ?: blockImageUrls.firstOrNull { !it.isNullOrBlank() }, "DRAFT", tags))
                                }.getOrDefault(CreateArticleResult.Fallback)
                                if (saveResult is CreateArticleResult.Success) {
                                    navController.navigate(Screen.Drafts.route) { popUpTo(Screen.EditArticle.route) { inclusive = true } }
                                }
                            }
                        }
                    )
                }
                state.isLoading -> EditLoadingText("Загрузка статьи...")
                else -> EditLoadingText("Редактирование доступно только автору статьи")
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
                },
                onOpenArticle = { postId ->
                    navController.navigate(Screen.ArticleDetails.createRoute(postId))
                }
            )
        }
    }
}


@Composable
private fun EditLoadingText(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text)
    }
}

private fun PostCardState.toRecipeEditDraft(): RecipeEditDraft = RecipeEditDraft(
    id = id,
    title = title,
    summary = description,
    imageUrl = imageUrl.takeIf { it.isNotBlank() },
    cookTimeMinutes = time.filter { it.isDigit() }.toIntOrNull(),
    proteinsPer100 = proteinsPer100.toDouble(),
    fatsPer100 = fatsPer100.toDouble(),
    carbsPer100 = carbsPer100.toDouble(),
    kcalPer100 = kcalPer100.toDouble(),
    tags = tags.map { it.title },
    ingredients = ingredients.map { it.text.toIngredientDraft() },
    steps = steps.mapIndexed { index, step ->
        RecipeStepDraft(
            number = step.title.filter { it.isDigit() }.toIntOrNull() ?: index + 1,
            description = step.description,
            photoUri = null,
            existingImageUrl = step.imageUrl
        )
    }
)

private fun String.toIngredientDraft(): IngredientDraft {
    val parts = split(" - ", limit = 2)
    val name = parts.firstOrNull()?.trim().orEmpty().ifBlank { "Ингредиент" }
    val amountParts = parts.getOrNull(1)?.trim()?.split(" ", limit = 2).orEmpty()
    val amount = amountParts.firstOrNull()?.replace(',', '.')?.toFloatOrNull() ?: 1f
    val unit = amountParts.getOrNull(1)?.trim().orEmpty().ifBlank { "шт" }
    return IngredientDraft(name, amount, unit)
}

private fun PostCardState.toArticleEditDraft(content: String): ArticleEditDraft = ArticleEditDraft(
    id = id,
    title = title,
    coverUrl = imageUrl.takeIf { it.isNotBlank() },
    tags = tags.map { it.title },
    blocks = content.toArticleBlocks()
)

private fun String.toArticleBlocks(): List<ArticleBlockDraft> {
    val chunks = split(Regex("(?m)(?=^##\\s+)"))
        .map { it.trim() }
        .filter { it.isNotBlank() }
    val parsed = chunks.mapIndexed { index, chunk ->
        val lines = chunk.lines()
        val title = lines.firstOrNull()?.removePrefix("##")?.trim().orEmpty().ifBlank { "Блок ${index + 1}" }
        val bodyLines = lines.drop(1)
        val imageLine = bodyLines.lastOrNull { it.trim().startsWith("[image:") && it.trim().endsWith("]") }
        val imageUrl = imageLine?.trim()?.removePrefix("[image:")?.removeSuffix("]")?.trim()
        val content = bodyLines.filterNot { it == imageLine }.joinToString("\n").trim()
        ArticleBlockDraft(index + 1, title, content, null, imageUrl)
    }
    return parsed.ifEmpty { listOf(ArticleBlockDraft(1, "Основной текст", trim(), null)) }
}

private fun List<ArticleBlockDraft>.toContentWithImages(imageUrls: List<String?>, fallbackContent: String): String =
    mapIndexed { index, block ->
        buildString {
            append("## ").append(block.title).append("\n")
            append(block.content)
            val imageUrl = imageUrls.getOrNull(index)
            if (!imageUrl.isNullOrBlank()) {
                append("\n[image:").append(imageUrl).append(']')
            }
        }
    }.joinToString("\n\n").ifBlank { fallbackContent }

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
            AppContainer.toRelativeMediaPath(api.uploadMedia(multipart).url)
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

        AppContainer.toRelativeMediaPath(api.uploadMedia(multipart).url)
    }.getOrNull()
}
