package com.example.core.data.repository

import com.example.core.common.coroutines.DispatcherProvider
import com.example.core.common.format.BrFormatters
import com.example.core.common.result.AppResult
import com.example.core.data.model.HomeSummary
import com.example.core.data.model.PointsSummary
import com.example.core.data.model.ReminderPriority
import com.example.core.data.model.ServiceReminder
import com.example.core.data.model.Vehicle
import com.example.core.data.model.VehicleSummary
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

@Singleton
class MockHomeRepository @Inject constructor(
  private val vehicleRepository: VehicleRepository,
  private val dispatchers: DispatcherProvider,
) : HomeRepository {

  private val refreshNonce = MutableStateFlow(0)

  override fun observeHomeSummary(): Flow<AppResult<HomeSummary>> =
    combine(
      vehicleRepository.observeVehicles(),
      refreshNonce,
    ) { vehicles, _ ->
      val primary = vehicles.firstOrNull { it.isPrimary } ?: vehicles.firstOrNull()
      AppResult.Success(buildSummary(primary)) as AppResult<HomeSummary>
    }
      .onStart {
        emit(AppResult.Loading)
        delay(250)
      }
      .flowOn(dispatchers.io)

  override suspend fun refreshHomeSummary(): AppResult<HomeSummary> =
    withContext(dispatchers.io) {
      delay(250)
      refreshNonce.value += 1
      val vehicles = vehicleRepository.observeVehicles().first()
      val latestPrimary = vehicles.firstOrNull { it.isPrimary } ?: vehicles.firstOrNull()
      AppResult.Success(buildSummary(latestPrimary))
    }

  private fun buildSummary(primaryVehicle: Vehicle?): HomeSummary {
    val vehicleSummary =
      primaryVehicle?.let { vehicle ->
        VehicleSummary(
          id = vehicle.id,
          plate = vehicle.plate,
          model = "${vehicle.brand} ${vehicle.model}",
          year = vehicle.year,
        )
      }

    val reminder =
      primaryVehicle?.let { vehicle ->
        ServiceReminder(
          id = "rem-${vehicle.id}",
          title = "Troca de óleo e filtro",
          description = "Recomendado a cada 10.000 km ou 6 meses para manter a garantia do motor.",
          vehicleLabel = "${vehicle.model} · ${BrFormatters.formatPlate(vehicle.plate)}",
          dueDate = LocalDate.now().plusDays(12),
          priority = ReminderPriority.DUE_SOON,
        )
      }

    return HomeSummary(
      customerFirstName = "Mariana",
      pointsSummary =
        PointsSummary(
          currentPoints = 720,
          nextRewardPoints = 1000,
          nextRewardTitle = "Lavagem completa grátis",
          cashbackAvailableCents = 2500L,
        ),
      primaryVehicle = vehicleSummary,
      nextReminder = reminder,
      pendingPaymentAmountCents = 18990L,
    )
  }
}
