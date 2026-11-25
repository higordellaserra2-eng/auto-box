package com.example.core.data.repository

import com.example.core.common.coroutines.DispatcherProvider
import com.example.core.common.result.AppResult
import com.example.core.data.model.HomeSummary
import com.example.core.data.model.PointsSummary
import com.example.core.data.model.ReminderPriority
import com.example.core.data.model.ServiceReminder
import com.example.core.data.model.VehicleSummary
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

@Singleton
class MockHomeRepository @Inject constructor(
  private val dispatchers: DispatcherProvider,
) : HomeRepository {

  private val currentSummary = MutableStateFlow(defaultMockSummary())

  override fun observeHomeSummary(): Flow<AppResult<HomeSummary>> =
    flow {
      emit(AppResult.Loading)
      delay(350)
      currentSummary.collect { summary ->
        emit(AppResult.Success(summary))
      }
    }.flowOn(dispatchers.io)

  override suspend fun refreshHomeSummary(): AppResult<HomeSummary> =
    withContext(dispatchers.io) {
      delay(350)
      val updated = defaultMockSummary()
      currentSummary.value = updated
      AppResult.Success(updated)
    }

  private fun defaultMockSummary(): HomeSummary =
    HomeSummary(
      customerFirstName = "Mariana",
      pointsSummary =
        PointsSummary(
          currentPoints = 720,
          nextRewardPoints = 1000,
          nextRewardTitle = "Lavagem completa grátis",
          cashbackAvailableCents = 2500L,
        ),
      primaryVehicle =
        VehicleSummary(
          id = "veh-01",
          plate = "BRA2E19",
          model = "Hyundai HB20 Comfort",
          year = 2023,
        ),
      nextReminder =
        ServiceReminder(
          id = "rem-01",
          title = "Troca de óleo e filtro",
          description = "Recomendado a cada 10.000 km ou 6 meses para manter a garantia do motor.",
          vehicleLabel = "HB20 · BRA2E19",
          dueDate = LocalDate.now().plusDays(12),
          priority = ReminderPriority.DUE_SOON,
        ),
      pendingPaymentAmountCents = 18990L,
    )
}
