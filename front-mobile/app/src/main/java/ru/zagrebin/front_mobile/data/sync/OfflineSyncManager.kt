package ru.zagrebin.front_mobile.data.sync

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.zagrebin.front_mobile.data.repository.FeedRepository
import ru.zagrebin.front_mobile.data.repository.StatisticsRepository
import ru.zagrebin.front_mobile.ui.navigation.AuthSessionState

class OfflineSyncManager(
    private val networkConnectionChecker: NetworkConnectionChecker,
    private val statisticsRepository: StatisticsRepository,
    private val feedRepository: FeedRepository
) {
    private val mutex = Mutex()

    suspend fun syncIfPossible(): Boolean = mutex.withLock {
        if (!AuthSessionState.isAuthorized.value || !networkConnectionChecker.isNetworkAvailable()) {
            return@withLock false
        }

        val statisticsSynced = statisticsRepository.syncPendingChanges()
        val draftsSynced = feedRepository.syncLocalDrafts()
        statisticsSynced && draftsSynced
    }
}
