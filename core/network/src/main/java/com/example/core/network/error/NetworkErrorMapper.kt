package com.example.core.network.error

import com.example.core.common.result.AppError
import com.example.core.common.result.AppResult
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

object NetworkErrorMapper {

  fun map(throwable: Throwable): AppError =
    when (throwable) {
      is UnknownHostException -> AppError.NetworkOffline
      is SocketTimeoutException -> AppError.Timeout
      is IOException -> AppError.NetworkOffline
      is HttpException ->
        when (throwable.code()) {
          401, 403 -> AppError.Unauthorized
          400, 422 -> AppError.Validation()
          408, 504 -> AppError.Timeout
          in 500..599 -> AppError.ServerUnavailable
          else -> AppError.Unknown()
        }
      else -> AppError.Unknown()
    }
}

suspend inline fun <T> safeApiCall(crossinline call: suspend () -> T): AppResult<T> =
  try {
    AppResult.Success(call())
  } catch (cancellation: CancellationException) {
    throw cancellation
  } catch (throwable: Throwable) {
    AppResult.Error(NetworkErrorMapper.map(throwable))
  }
