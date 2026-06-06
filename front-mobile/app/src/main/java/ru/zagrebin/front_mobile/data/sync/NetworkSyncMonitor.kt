package ru.zagrebin.front_mobile.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class NetworkSyncMonitor(
    context: Context,
    private val scope: CoroutineScope,
    private val syncManager: OfflineSyncManager
) {
    private val connectivityManager = context.applicationContext
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            scope.launch { syncManager.syncIfPossible() }
        }
    }

    fun start() {
        runCatching { connectivityManager.registerDefaultNetworkCallback(callback) }
    }

    fun stop() {
        runCatching { connectivityManager.unregisterNetworkCallback(callback) }
    }
}
