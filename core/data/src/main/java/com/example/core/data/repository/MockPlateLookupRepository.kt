package com.example.core.data.repository

import com.example.core.common.connectivity.ConnectivityMonitor
import com.example.core.common.coroutines.DispatcherProvider
import com.example.core.common.format.BrFormatters
import com.example.core.common.result.AppError
import com.example.core.common.result.AppResult
import com.example.core.data.model.PlateLookupInfo
import com.example.core.data.model.PlateLookupResult
import com.example.core.data.model.VehicleType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@Singleton
class MockPlateLookupRepository @Inject constructor(
  private val connectivityMonitor: ConnectivityMonitor,
  private val dispatchers: DispatcherProvider,
) : PlateLookupRepository {

  private val directory: Map<String, PlateLookupInfo> =
    mapOf(
      "BRA2E19" to
        PlateLookupInfo(
          plate = "BRA2E19",
          type = VehicleType.CAR,
          brand = "Hyundai",
          model = "HB20",
          year = 2023,
          color = "Prata",
        ),
      "ABC1234" to
        PlateLookupInfo(
          plate = "ABC1234",
          type = VehicleType.CAR,
          brand = "Chevrolet",
          model = "Onix",
          year = 2022,
          color = "Branco",
        ),
      "FGH4I56" to
        PlateLookupInfo(
          plate = "FGH4I56",
          type = VehicleType.CAR,
          brand = "Fiat",
          model = "Strada",
          year = 2024,
          color = "Vermelho",
        ),
      "JKL8M90" to
        PlateLookupInfo(
          plate = "JKL8M90",
          type = VehicleType.CAR,
          brand = "Volkswagen",
          model = "Polo",
          year = 2023,
          color = "Cinza",
        ),
      "BYD2D25" to
        PlateLookupInfo(
          plate = "BYD2D25",
          type = VehicleType.CAR,
          brand = "BYD",
          model = "Dolphin",
          year = 2025,
          color = "Azul",
        ),
      "MOT1A23" to
        PlateLookupInfo(
          plate = "MOT1A23",
          type = VehicleType.MOTORCYCLE,
          brand = "Honda",
          model = "CG 160 Titan",
          year = 2024,
          color = "Preto",
        ),
    )

  override suspend fun lookupByPlate(plate: String): AppResult<PlateLookupResult> =
    withContext(dispatchers.io) {
      val normalized = BrFormatters.normalizePlate(plate)
      if (!BrFormatters.isValidPlate(normalized)) {
        return@withContext AppResult.Error(AppError.Validation())
      }

      delay(550L)

      val isOnline = connectivityMonitor.isOnline.first()
      if (!isOnline) {
        return@withContext AppResult.Error(AppError.NetworkOffline)
      }

      if (normalized.startsWith("ERR")) {
        return@withContext AppResult.Error(AppError.ServerUnavailable)
      }
      if (normalized.startsWith("TMO")) {
        return@withContext AppResult.Error(AppError.Timeout)
      }

      val found = directory[normalized]
      if (found != null) {
        AppResult.Success(PlateLookupResult.Found(found))
      } else {
        AppResult.Success(PlateLookupResult.NotFound)
      }
    }
}
