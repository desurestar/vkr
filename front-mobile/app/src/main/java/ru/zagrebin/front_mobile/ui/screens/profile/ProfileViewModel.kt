package ru.zagrebin.front_mobile.ui.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.data.AppContainer
import ru.zagrebin.front_mobile.data.remote.api.UserProfileDto
import ru.zagrebin.front_mobile.domain.model.ProfileData
import ru.zagrebin.front_mobile.ui.navigation.AuthSessionState
import ru.zagrebin.front_mobile.ui.screens.profile.components.ProfileEvent

private const val USERS_SEARCH_DEBOUNCE_MS = 300L

private data class ProfileUsersSheetState(
    val type: ProfileUsersSheetType? = null,
    val query: String = "",
    val users: List<ProfileUserItemState> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppContainer(application).let {
        ProfileRepository(it.feedApi, it.db.profileDao(), it.networkConnectionChecker)
    }
    private val profile = MutableStateFlow<ProfileData?>(null)
    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val usersSheet = MutableStateFlow(ProfileUsersSheetState())
    private var usersSearchJob: Job? = null

    val state: StateFlow<ProfileState> = combine(
        profile,
        isRefreshing,
        errorMessage,
        usersSheet
    ) { loadedProfile, refreshing, error, sheet ->
        val baseState = loadedProfile?.toState(isLoading = false, error = error)
            ?: ProfileState(isLoading = refreshing, error = error)
        baseState.copy(
            usersSheetType = sheet.type,
            usersSearchQuery = sheet.query,
            users = sheet.users,
            isUsersLoading = sheet.isLoading,
            usersError = sheet.error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileState())

    init {
        if (AuthSessionState.isAuthorized.value) loadProfile()
    }

    fun loadProfile() {
        if (!AuthSessionState.isAuthorized.value) {
            profile.value = null
            isRefreshing.value = false
            errorMessage.value = null
            closeUsersSheet()
            return
        }
        viewModelScope.launch {
            isRefreshing.value = true
            errorMessage.value = null
            repository.getCachedProfile()?.let { cached ->
                profile.value = cached
                errorMessage.value = "Показан офлайн-кеш. Обновление выполняется в фоне."
            }
            runCatching { repository.getMyProfile() }
                .onSuccess { result ->
                    profile.value = result.profile
                    errorMessage.value = if (result.isFromCache) "Сервер недоступен. Показан офлайн-кеш." else null
                }
                .onFailure { error ->
                    errorMessage.value = error.message ?: "Не удалось загрузить профиль"
                }
            isRefreshing.value = false
        }
    }

    fun openFollowingSheet() = openUsersSheet(ProfileUsersSheetType.Following)

    fun openFollowersSheet() = openUsersSheet(ProfileUsersSheetType.Followers)

    fun closeUsersSheet() {
        usersSearchJob?.cancel()
        usersSheet.value = ProfileUsersSheetState()
    }

    fun onUsersSearchChange(query: String) {
        usersSheet.value = usersSheet.value.copy(query = query)
        loadUsers(query, debounce = true)
    }

    fun onEvent(event: ProfileEvent) { }

    private fun openUsersSheet(type: ProfileUsersSheetType) {
        if (profile.value == null) return
        usersSheet.value = ProfileUsersSheetState(type = type, isLoading = true)
        loadUsers(query = "", debounce = false)
    }

    private fun loadUsers(query: String, debounce: Boolean) {
        val currentProfile = profile.value ?: return
        val sheetType = usersSheet.value.type ?: return
        usersSearchJob?.cancel()
        usersSearchJob = viewModelScope.launch {
            if (debounce) delay(USERS_SEARCH_DEBOUNCE_MS)
            usersSheet.value = usersSheet.value.copy(isLoading = true, error = null)
            runCatching {
                when (sheetType) {
                    ProfileUsersSheetType.Following -> repository.getFollowingUsers(currentProfile.id, query)
                    ProfileUsersSheetType.Followers -> repository.getFollowerUsers(currentProfile.id, query)
                }
            }.onSuccess { users ->
                usersSheet.value = usersSheet.value.copy(
                    users = users.map { it.toProfileUserItemState() },
                    isLoading = false,
                    error = null
                )
            }.onFailure { throwable ->
                usersSheet.value = usersSheet.value.copy(
                    users = emptyList(),
                    isLoading = false,
                    error = throwable.message ?: "Не удалось загрузить пользователей"
                )
            }
        }
    }
}

private fun ProfileData.toState(isLoading: Boolean, error: String?) = ProfileState(
    isLoading = isLoading,
    userId = id,
    name = name,
    email = email,
    bio = bio,
    avatarUrl = avatarUrl,
    following = followingCount.toString(),
    followers = followersCount.toString(),
    likes = totalLikes.toString(),
    error = error
)

private fun UserProfileDto.toProfileUserItemState(): ProfileUserItemState = ProfileUserItemState(
    id = id,
    username = username.orEmpty(),
    displayName = displayName?.takeIf { it.isNotBlank() } ?: username.orEmpty(),
    avatarUrl = avatarUrl
)
