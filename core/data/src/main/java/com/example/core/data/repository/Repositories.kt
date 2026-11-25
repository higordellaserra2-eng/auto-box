package com.example.core.data.repository

import com.example.core.common.result.AppResult
import com.example.core.data.model.HomeSummary
import com.example.core.data.model.PlateLookupResult
import com.example.core.data.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
  fun observeHomeSummary(): Flow<AppResult<HomeSummary>>

  suspend fun refreshHomeSummary(): AppResult<HomeSummary>
}

interface UserPreferencesRepository {
  val hasCompletedOnboarding: Flow<Boolean>

  suspend fun setOnboardingCompleted(completed: Boolean)
}

interface VehicleRepository {
  fun observeVehicles(): Flow<List<Vehicle>>

  suspend fun getVehicleById(id: String): Vehicle?

  suspend fun addVehicle(vehicle: Vehicle): AppResult<Vehicle>

  suspend fun updateVehicle(vehicle: Vehicle): AppResult<Vehicle>

  suspend fun deleteVehicle(id: String): AppResult<Vehicle?>

  suspend fun restoreVehicle(vehicle: Vehicle): AppResult<Vehicle>

  suspend fun setPrimaryVehicle(id: String): AppResult<Unit>
}

interface PlateLookupRepository {
  suspend fun lookupByPlate(plate: String): AppResult<PlateLookupResult>
}
