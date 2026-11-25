package com.example.core.network.di

import com.example.core.network.BuildConfig
import com.example.core.network.auth.AuthTokenProvider
import com.example.core.network.auth.MockAuthTokenProvider
import com.example.core.network.interceptor.AuthInterceptor
import com.example.core.network.interceptor.SafeLoggingInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindModule {
  @Binds
  @Singleton
  abstract fun bindAuthTokenProvider(impl: MockAuthTokenProvider): AuthTokenProvider
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkProvideModule {

  @Provides
  @Singleton
  fun provideMoshi(): Moshi =
    Moshi.Builder()
      .addLast(KotlinJsonAdapterFactory())
      .build()

  @Provides
  @Singleton
  fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
    OkHttpClient.Builder()
      .connectTimeout(15, TimeUnit.SECONDS)
      .readTimeout(20, TimeUnit.SECONDS)
      .writeTimeout(20, TimeUnit.SECONDS)
      .addInterceptor(authInterceptor)
      .addInterceptor(SafeLoggingInterceptor.create())
      .build()

  @Provides
  @Singleton
  fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
    Retrofit.Builder()
      .baseUrl(BuildConfig.API_BASE_URL)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
}
