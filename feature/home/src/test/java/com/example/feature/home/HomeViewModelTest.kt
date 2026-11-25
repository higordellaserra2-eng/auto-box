package com.example.feature.home

import app.cash.turbine.test
import com.example.core.common.connectivity.ConnectivityMonitor
import com.example.core.common.result.AppError
import com.example.core.common.result.AppResult
import com.example.core.data.model.HomeSummary
import com.example.core.data.model.PointsSummary
import com.example.core.data.model.ReminderPriority
import com.example.core.data.model.ServiceReminder
import com.example.core.data.model.VehicleSummary
import com.example.core.data.repository.HomeRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  private val sampleSummary =
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
          id = "v1",
          plate = "BRA2E19",
          model = "Hyundai HB20",
          year = 2023,
        ),
      nextReminder =
        ServiceReminder(
          id = "r1",
          title = "Troca de óleo",
          description = "Revisão periódica",
          vehicleLabel = "HB20 · BRA2E19",
          dueDate = LocalDate.of(2026, 12, 7),
          priority = ReminderPriority.DUE_SOON,
        ),
      pendingPaymentAmountCents = 18990L,
    )

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `uiState transitions from Loading to Success and recovers from Error on retry`() =
    runTest(testDispatcher) {
      val repoFlow = MutableStateFlow<AppResult<HomeSummary>>(AppResult.Loading)
      val repository = mockk<HomeRepository>()
      every { repository.observeHomeSummary() } returns repoFlow
      coEvery { repository.refreshHomeSummary() } coAnswers {
        repoFlow.value = AppResult.Success(sampleSummary)
        AppResult.Success(sampleSummary)
      }

      val connectivity = mockk<ConnectivityMonitor>()
      every { connectivity.isOnline } returns flowOf(true)

      val viewModel = HomeViewModel(repository, connectivity)

      viewModel.uiState.test {
        assertEquals(HomeUiState.Loading, awaitItem())

        repoFlow.value = AppResult.Error(AppError.NetworkOffline)
        val errorState = awaitItem()
        assertTrue(errorState is HomeUiState.Error)

        viewModel.retry()
        testScheduler.advanceUntilIdle()

        val current = expectMostRecentItem()
        assertTrue(current is HomeUiState.Success)
        assertEquals("Mariana", (current as HomeUiState.Success).summary.customerFirstName)
      }
    }

  @Test
  fun `isOffline reflects ConnectivityMonitor updates`() = runTest(testDispatcher) {
    val repository = mockk<HomeRepository>()
    every { repository.observeHomeSummary() } returns flowOf(AppResult.Success(sampleSummary))

    val onlineFlow = MutableStateFlow(true)
    val connectivity = mockk<ConnectivityMonitor>()
    every { connectivity.isOnline } returns onlineFlow

    val viewModel = HomeViewModel(repository, connectivity)

    viewModel.isOffline.test {
      assertEquals(false, awaitItem())
      onlineFlow.value = false
      assertEquals(true, awaitItem())
    }
  }
}
