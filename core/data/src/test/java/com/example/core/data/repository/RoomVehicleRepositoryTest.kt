package com.example.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.common.coroutines.DispatcherProvider
import com.example.core.common.result.AppError
import com.example.core.common.result.AppResult
import com.example.core.data.local.AutoBoxDatabase
import com.example.core.data.model.Vehicle
import com.example.core.data.model.VehicleType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomVehicleRepositoryTest {

  private lateinit var database: AutoBoxDatabase
  private lateinit var repository: RoomVehicleRepository
  private lateinit var homeRepository: MockHomeRepository

  private val testDispatcher = UnconfinedTestDispatcher()
  private val dispatchers =
    object : DispatcherProvider {
      override val io: CoroutineDispatcher = testDispatcher
      override val default: CoroutineDispatcher = testDispatcher
      override val main: CoroutineDispatcher = testDispatcher
    }

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database =
      Room.inMemoryDatabaseBuilder(context, AutoBoxDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    repository = RoomVehicleRepository(database.vehicleDao(), dispatchers)
    homeRepository = MockHomeRepository(repository, dispatchers)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `first vehicle becomes primary automatically and duplicate plate is rejected`() = runTest(testDispatcher) {
    val firstResult =
      repository.addVehicle(
        Vehicle(
          id = "v1",
          plate = "bra-2e19",
          type = VehicleType.CAR,
          brand = "Hyundai",
          model = "HB20",
          year = 2023,
          isPrimary = false,
          createdAtEpochMillis = 100L,
        ),
      )

    assertTrue(firstResult is AppResult.Success)
    val first = (firstResult as AppResult.Success).data
    assertEquals("BRA2E19", first.plate)
    assertTrue(first.isPrimary)

    val duplicateResult =
      repository.addVehicle(
        Vehicle(
          id = "v2",
          plate = "BRA2E19",
          type = VehicleType.CAR,
          brand = "Fiat",
          model = "Argo",
          year = 2024,
          isPrimary = false,
          createdAtEpochMillis = 200L,
        ),
      )

    assertTrue(duplicateResult is AppResult.Error)
    assertTrue((duplicateResult as AppResult.Error).error is AppError.Validation)
  }

  @Test
  fun `deleting primary promotes most recent vehicle and restore recovers primary`() = runTest(testDispatcher) {
    repository.addVehicle(
      Vehicle(
        id = "v1",
        plate = "BRA2E19",
        type = VehicleType.CAR,
        brand = "Hyundai",
        model = "HB20",
        year = 2023,
        isPrimary = true,
        createdAtEpochMillis = 100L,
      ),
    )
    repository.addVehicle(
      Vehicle(
        id = "v2",
        plate = "ABC1234",
        type = VehicleType.CAR,
        brand = "Chevrolet",
        model = "Onix",
        year = 2022,
        isPrimary = false,
        createdAtEpochMillis = 200L,
      ),
    )

    val deleteResult = repository.deleteVehicle("v1")
    assertTrue(deleteResult is AppResult.Success)
    val deleted = (deleteResult as AppResult.Success).data
    assertNotNull(deleted)

    val afterDelete = repository.observeVehicles().first()
    assertEquals(1, afterDelete.size)
    assertEquals("v2", afterDelete.first().id)
    assertTrue(afterDelete.first().isPrimary)

    val restoreResult = repository.restoreVehicle(deleted!!)
    assertTrue(restoreResult is AppResult.Success)

    val afterRestore = repository.observeVehicles().first()
    assertEquals(2, afterRestore.size)
    assertEquals("v1", afterRestore.first { it.isPrimary }.id)
  }

  @Test
  fun `homeRepository derives primaryVehicle from vehicleRepository`() = runTest(testDispatcher) {
    val emptySummary = homeRepository.refreshHomeSummary()
    assertTrue(emptySummary is AppResult.Success)
    assertNull((emptySummary as AppResult.Success).data.primaryVehicle)

    repository.addVehicle(
      Vehicle(
        id = "v1",
        plate = "FGH4I56",
        type = VehicleType.CAR,
        brand = "Fiat",
        model = "Strada",
        year = 2024,
        isPrimary = true,
      ),
    )

    val updatedSummary = homeRepository.refreshHomeSummary()
    assertTrue(updatedSummary is AppResult.Success)
    val summary = (updatedSummary as AppResult.Success).data
    assertEquals("FGH4I56", summary.primaryVehicle?.plate)
    assertEquals("Fiat Strada", summary.primaryVehicle?.model)
  }
}
