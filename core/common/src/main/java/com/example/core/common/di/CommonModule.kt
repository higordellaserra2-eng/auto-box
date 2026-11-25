package com.example.core.common.di

import com.example.core.common.connectivity.ConnectivityMonitor
import com.example.core.common.connectivity.NetworkConnectivityMonitor
import com.example.core.common.coroutines.DefaultDispatcher
import com.example.core.common.coroutines.DefaultDispatcherProvider
import com.example.core.common.coroutines.DispatcherProvider
import com.example.core.common.coroutines.IoDispatcher
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
abstract class CommonBindModule {
  @Binds
  @Singleton
  abstract fun bindConnectivityMonitor(
    impl: NetworkConnectivityMonitor,
  ): ConnectivityMonitor
}

@Module
@InstallIn(SingletonComponent::class)
object CommonProvideModule {
  @Provides
  @IoDispatcher
  fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

  @Provides
  @DefaultDispatcher
  fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

  @Provides
  @Singleton
  fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
}
