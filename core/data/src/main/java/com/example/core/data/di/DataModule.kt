package com.example.core.data.di

import com.example.core.data.repository.DataStoreUserPreferencesRepository
import com.example.core.data.repository.HomeRepository
import com.example.core.data.repository.MockHomeRepository
import com.example.core.data.repository.MockPlateLookupRepository
import com.example.core.data.repository.PlateLookupRepository
import com.example.core.data.repository.RoomVehicleRepository
import com.example.core.data.repository.UserPreferencesRepository
import com.example.core.data.repository.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

  @Binds
  @Singleton
  abstract fun bindHomeRepository(impl: MockHomeRepository): HomeRepository

  @Binds
  @Singleton
  abstract fun bindUserPreferencesRepository(
    impl: DataStoreUserPreferencesRepository,
  ): UserPreferencesRepository

  @Binds
  @Singleton
  abstract fun bindVehicleRepository(
    impl: RoomVehicleRepository,
  ): VehicleRepository

  @Binds
  @Singleton
  abstract fun bindPlateLookupRepository(
    impl: MockPlateLookupRepository,
  ): PlateLookupRepository
}
