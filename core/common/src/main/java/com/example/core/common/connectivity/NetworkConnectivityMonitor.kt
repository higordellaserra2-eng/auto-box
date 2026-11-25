package com.example.core.common.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

@Singleton
class NetworkConnectivityMonitor @Inject constructor(
  @ApplicationContext private val context: Context,
) : ConnectivityMonitor {

  override val isOnline: Flow<Boolean> = callbackFlow {
    val connectivityManager = context.getSystemService<ConnectivityManager>()
    if (connectivityManager == null) {
      trySend(false)
      close()
      return@callbackFlow
    }

    val callback =
      object : ConnectivityManager.NetworkCallback() {
        private val networks = mutableSetOf<Network>()

        override fun onAvailable(network: Network) {
          networks += network
          trySend(true)
        }

        override fun onLost(network: Network) {
          networks -= network
          trySend(networks.isNotEmpty() || connectivityManager.isCurrentlyConnected())
        }

        override fun onCapabilitiesChanged(
          network: Network,
          networkCapabilities: NetworkCapabilities,
        ) {
          val hasInternet =
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
              networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
          if (hasInternet) {
            networks += network
          } else {
            networks -= network
          }
          trySend(networks.isNotEmpty())
        }
      }

    val request =
      NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    trySend(connectivityManager.isCurrentlyConnected())
    connectivityManager.registerNetworkCallback(request, callback)

    awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
  }
    .distinctUntilChanged()
    .conflate()

  private fun ConnectivityManager.isCurrentlyConnected(): Boolean {
    val active = activeNetwork ?: return false
    val capabilities = getNetworkCapabilities(active) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }
}
