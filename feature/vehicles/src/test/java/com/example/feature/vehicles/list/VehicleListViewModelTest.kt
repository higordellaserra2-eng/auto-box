package com.example.feature.vehicles.list

import app.cash.turbine.test
import com.example.core.common.connectivity.ConnectivityMonitor
import com.example.core.common.result.AppResult
import com.example.core.data.model.Vehicle
import com.example.core.data.model.VehicleType
import com.example.core.data.repository.VehicleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleListViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  private val sampleCar =
    Vehicle(
      id = "v1",
      plate = "BRA2E19",
      type = VehicleType.CAR,
      brand = "Hyundai",
      model = "HB20",
      year = 2023,
      isPrimary = true,
    )

  private val sampleMoto =
    Vehicle(
      id = "v2",
      plate = "MOT1A23",
      type = VehicleType.MOTORCYCLE,
      brand = "Honda",
      model = "CG 160 Titan",
      year = 2024,
      isPrimary = false,
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
  fun `delete and undo restores vehicle and emits expected events`() = runTest(testDispatcher) {
    val vehiclesFlow = MutableStateFlow(listOf(sampleCar, sampleMoto))
    val repository = mockk<VehicleRepository>()
    every { repository.observeVehicles() } returns vehiclesFlow
    coEvery { repository.deleteVehicle("v1") } coAnswers {
      vehiclesFlow.value = listOf(sampleMoto.copy(isPrimary = true))
      AppResult.Success(sampleCar)
    }
    coEvery { repository.restoreVehicle(sampleCar) } coAnswers {
      vehiclesFlow.value = listOf(sampleCar, sampleMoto)
      AppResult.Success(sampleCar)
    }

    val connectivity = mockk<ConnectivityMonitor>()
    every { connectivity.isOnline } returns flowOf(true)

    val viewModel = VehicleListViewModel(repository, connectivity)

    viewModel.uiState.test {
      assertEquals(VehicleListUiState.Loading, awaitItem())
      val loaded = awaitItem() as VehicleListUiState.Success
      assertEquals(2, loaded.vehicles.size)

      viewModel.requestDelete(sampleCar)
      val pending = awaitItem() as VehicleListUiState.Success
      assertEquals("v1", pending.vehiclePendingDeletion?.id)

      viewModel.confirmDelete()
      advanceUntilIdle()

      val afterDelete = expectMostRecentItem() as VehicleListUiState.Success
      assertNull(afterDelete.vehiclePendingDeletion)
      assertEquals(1, afterDelete.vehicles.size)
    }

    viewModel.events.test {
      val undoEvent = awaitItem()
      assertTrue(undoEvent is VehicleListEvent.ShowUndoDeleteSnackbar)
      val deleted = (undoEvent as VehicleListEvent.ShowUndoDeleteSnackbar).deletedVehicle

      viewModel.undoDelete(deleted)
      advanceUntilIdle()
      val restoredEvent = awaitItem()
      assertTrue(restoredEvent is VehicleListEvent.ShowSnackbar)
    }

    coVerify(exactly = 1) { repository.deleteVehicle("v1") }
    coVerify(exactly = 1) { repository.restoreVehicle(sampleCar) }
  }

  @Test
  fun `setPrimaryVehicle invokes repository and emits success snackbar`() =
    runTest(testDispatcher) {
      val repository = mockk<VehicleRepository>()
      every { repository.observeVehicles() } returns flowOf(listOf(sampleCar, sampleMoto))
      coEvery { repository.setPrimaryVehicle("v2") } returns AppResult.Success(Unit)

      val connectivity = mockk<ConnectivityMonitor>()
      every { connectivity.isOnline } returns flowOf(true)

      val viewModel = VehicleListViewModel(repository, connectivity)

      viewModel.events.test {
        viewModel.setPrimaryVehicle(sampleMoto)
        advanceUntilIdle()

        val event = awaitItem()
        assertTrue(event is VehicleListEvent.ShowSnackbar)
      }

      coVerify(exactly = 1) { repository.setPrimaryVehicle("v2") }
    }
}
