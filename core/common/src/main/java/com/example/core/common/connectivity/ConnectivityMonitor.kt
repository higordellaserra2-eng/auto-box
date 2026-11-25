package com.example.core.common.connectivity

import kotlinx.coroutines.flow.Flow

interface ConnectivityMonitor {
  val isOnline: Flow<Boolean>
}
