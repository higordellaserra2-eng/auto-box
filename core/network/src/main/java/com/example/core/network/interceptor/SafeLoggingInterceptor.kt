package com.example.core.network.interceptor

import com.example.core.network.BuildConfig
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor

object SafeLoggingInterceptor {

  fun create(): Interceptor {
    val logger =
      HttpLoggingInterceptor().apply {
        // Never log request/response bodies to avoid leaking PII, tokens, or payment fields.
        level =
          if (BuildConfig.ENABLE_SAFE_HTTP_LOGS) {
            HttpLoggingInterceptor.Level.HEADERS
          } else {
            HttpLoggingInterceptor.Level.NONE
          }
        redactHeader("Authorization")
        redactHeader("Cookie")
        redactHeader("Set-Cookie")
        redactHeader("X-Api-Key")
      }
    return logger
  }
}
