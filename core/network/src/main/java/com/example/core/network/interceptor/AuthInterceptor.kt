package com.example.core.network.interceptor

import com.example.core.network.auth.AuthTokenProvider
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor @Inject constructor(
  private val tokenProvider: AuthTokenProvider,
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val original = chain.request()
    val token = runBlocking { tokenProvider.getAccessToken() }
    val request =
      original
        .newBuilder()
        .header("Accept", "application/json")
        .header("Accept-Language", "pt-BR")
        .header("X-Client-Platform", "android")
        .apply {
          if (!token.isNullOrBlank()) {
            header("Authorization", "Bearer $token")
          }
        }
        .build()

    return chain.proceed(request)
  }
}
