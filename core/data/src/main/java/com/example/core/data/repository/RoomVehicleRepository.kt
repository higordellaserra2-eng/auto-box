package com.example.core.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.core.common.coroutines.DispatcherProvider
import com.example.core.common.format.BrFormatters
import com.example.core.common.result.AppError
import com.example.core.common.result.AppResult
import com.example.core.common.text.UiText
import com.example.core.data.R
import com.example.core.data.local.VehicleDao
import com.example.core.data.local.VehicleEntity
import com.example.core.data.model.Vehicle
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class RoomVehicleRepository @Inject constructor(
  private val vehicleDao: VehicleDao,
  private val dispatchers: DispatcherProvider,
) : VehicleRepository {

  override fun observeVehicles(): Flow<List<Vehicle>> =
    vehicleDao
      .observeVehicles()
      .map { entities -> entities.map { it.toDomain() } }
      .flowOn(dispatchers.io)

  override suspend fun getVehicleById(id: String): Vehicle? =
    withContext(dispatchers.io) {
      vehicleDao.getById(id)?.toDomain()
    }

  override suspend fun addVehicle(vehicle: Vehicle): AppResult<Vehicle> =
    withContext(dispatchers.io) {
      val normalizedPlate = BrFormatters.normalizePlate(vehicle.plate)
      if (!BrFormatters.isValidPlate(normalizedPlate)) {
        return@withContext AppResult.Error(
          AppError.Validation(UiText.StringResource(R.string.vehicle_error_invalid_plate)),
        )
      }

      val existingWithPlate = vehicleDao.getByPlate(normalizedPlate)
      if (existingWithPlate != null) {
        return@withContext duplicatePlateError(normalizedPlate)
      }

      val id = vehicle.id.ifBlank { UUID.randomUUID().toString() }
      val now = System.currentTimeMillis()
      val entity =
        VehicleEntity.fromDomain(
          vehicle =
            vehicle.copy(
              id = id,
              plate = normalizedPlate,
              brand = vehicle.brand.trim(),
              model = vehicle.model.trim(),
              color = vehicle.color?.trim()?.takeIf { it.isNotEmpty() },
              nickname = vehicle.nickname?.trim()?.takeIf { it.isNotEmpty() },
              createdAtEpochMillis = if (vehicle.createdAtEpochMillis > 0L) vehicle.createdAtEpochMillis else now,
            ),
          updatedAtEpochMillis = now,
        )

      try {
        val inserted = vehicleDao.insertVehicleTransaction(entity)
        AppResult.Success(inserted.toDomain())
      } catch (_: SQLiteConstraintException) {
        duplicatePlateError(normalizedPlate)
      }
    }

  override suspend fun updateVehicle(vehicle: Vehicle): AppResult<Vehicle> =
    withContext(dispatchers.io) {
      val existing = vehicleDao.getById(vehicle.id)
        ?: return@withContext AppResult.Error(
          AppError.Validation(UiText.StringResource(R.string.vehicle_error_not_found)),
        )

      val normalizedPlate = BrFormatters.normalizePlate(vehicle.plate)
      if (!BrFormatters.isValidPlate(normalizedPlate)) {
        return@withContext AppResult.Error(
          AppError.Validation(UiText.StringResource(R.string.vehicle_error_invalid_plate)),
        )
      }

      val duplicate = vehicleDao.getByPlate(normalizedPlate)
      if (duplicate != null && duplicate.id != vehicle.id) {
        return@withContext duplicatePlateError(normalizedPlate)
      }

      val entity =
        VehicleEntity.fromDomain(
          vehicle =
            vehicle.copy(
              plate = normalizedPlate,
              brand = vehicle.brand.trim(),
              model = vehicle.model.trim(),
              color = vehicle.color?.trim()?.takeIf { it.isNotEmpty() },
              nickname = vehicle.nickname?.trim()?.takeIf { it.isNotEmpty() },
              createdAtEpochMillis = existing.createdAtEpochMillis,
            ),
          updatedAtEpochMillis = System.currentTimeMillis(),
        )

      try {
        val updated = vehicleDao.updateVehicleTransaction(entity)
        AppResult.Success(updated.toDomain())
      } catch (_: SQLiteConstraintException) {
        duplicatePlateError(normalizedPlate)
      }
    }

  override suspend fun deleteVehicle(id: String): AppResult<Vehicle?> =
    withContext(dispatchers.io) {
      val deleted = vehicleDao.deleteAndPromoteTransaction(id)
      if (deleted == null) {
        AppResult.Error(AppError.Validation(UiText.StringResource(R.string.vehicle_error_not_found)))
      } else {
        AppResult.Success(deleted.toDomain())
      }
    }

  override suspend fun restoreVehicle(vehicle: Vehicle): AppResult<Vehicle> =
    withContext(dispatchers.io) {
      val normalizedPlate = BrFormatters.normalizePlate(vehicle.plate)
      val duplicate = vehicleDao.getByPlate(normalizedPlate)
      if (duplicate != null && duplicate.id != vehicle.id) {
        return@withContext duplicatePlateError(normalizedPlate)
      }
      val entity =
        VehicleEntity.fromDomain(
          vehicle = vehicle.copy(plate = normalizedPlate),
          updatedAtEpochMillis = System.currentTimeMillis(),
        )
      val restored = vehicleDao.restoreVehicleTransaction(entity)
      AppResult.Success(restored.toDomain())
    }

  override suspend fun setPrimaryVehicle(id: String): AppResult<Unit> =
    withContext(dispatchers.io) {
      val existing = vehicleDao.getById(id)
      if (existing == null) {
        AppResult.Error(AppError.Validation(UiText.StringResource(R.string.vehicle_error_not_found)))
      } else {
        vehicleDao.setPrimaryTransaction(id)
        AppResult.Success(Unit)
      }
    }

  private fun duplicatePlateError(normalizedPlate: String): AppResult.Error =
    AppResult.Error(
      AppError.Validation(
        UiText.StringResource(
          R.string.vehicle_error_duplicate_plate,
          BrFormatters.formatPlate(normalizedPlate),
        ),
      ),
    )
}
