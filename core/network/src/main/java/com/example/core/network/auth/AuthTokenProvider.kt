package com.example.core.network.auth

import javax.inject.Inject
import javax.inject.Singleton

interface AuthTokenProvider {
  suspend fun getAccessToken(): String?
}

@Singleton
class MockAuthTokenProvider @Inject constructor() : AuthTokenProvider {
  override suspend fun getAccessToken(): String? = null
}
