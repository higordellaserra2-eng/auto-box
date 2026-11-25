package com.example.core.common.result

import com.example.core.common.R
import com.example.core.common.text.UiText

sealed interface AppResult<out T> {
  data object Loading : AppResult<Nothing>

  data class Success<out T>(val data: T) : AppResult<T>

  data class Error(val error: AppError) : AppResult<Nothing>
}

sealed interface AppError {
  val userMessage: UiText

  data object NetworkOffline : AppError {
    override val userMessage: UiText = UiText.StringResource(R.string.error_offline)
  }

  data object Timeout : AppError {
    override val userMessage: UiText = UiText.StringResource(R.string.error_timeout)
  }

  data object Unauthorized : AppError {
    override val userMessage: UiText = UiText.StringResource(R.string.error_unauthorized)
  }

  data object ServerUnavailable : AppError {
    override val userMessage: UiText = UiText.StringResource(R.string.error_server_unavailable)
  }

  data class Validation(
    override val userMessage: UiText = UiText.StringResource(R.string.error_validation),
  ) : AppError

  data class Unknown(
    override val userMessage: UiText = UiText.StringResource(R.string.error_unknown),
  ) : AppError
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> =
  when (this) {
    AppResult.Loading -> AppResult.Loading
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Error -> this
  }
