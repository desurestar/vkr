package ru.zagrebin.front_mobile.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.zagrebin.front_mobile.ui.navigation.AuthSessionState

class NetworkSyncMonitor(
    context: Context,
    private val scope: CoroutineScope,
    private val syncManager: OfflineSyncManager
) {
    private var authSyncJob: Job? = null

    private val connectivityManager = context.applicationContext
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            scope.launch { syncManager.syncIfPossible() }
        }
    }

    fun start() {
        runCatching { connectivityManager.registerDefaultNetworkCallback(callback) }
        authSyncJob?.cancel()
        authSyncJob = scope.launch {
            AuthSessionState.isAuthorized.collect { isAuthorized ->
                if (isAuthorized) {
                    syncManager.syncIfPossible()
                }
            }
        }
        scope.launch { syncManager.syncIfPossible() }
    }

    fun stop() {
        authSyncJob?.cancel()
        authSyncJob = null
        runCatching { connectivityManager.unregisterNetworkCallback(callback) }
    }
}
