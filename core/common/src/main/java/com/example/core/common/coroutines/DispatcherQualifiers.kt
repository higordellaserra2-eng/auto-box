package com.example.core.common.coroutines

import javax.inject.Qualifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

interface DispatcherProvider {
  val io: CoroutineDispatcher
  val default: CoroutineDispatcher
  val main: CoroutineDispatcher
}

class DefaultDispatcherProvider : DispatcherProvider {
  override val io: CoroutineDispatcher = Dispatchers.IO
  override val default: CoroutineDispatcher = Dispatchers.Default
  override val main: CoroutineDispatcher = Dispatchers.Main
}
