package com.example.feature.vehicles.form

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.core.common.format.PlatePattern
import com.example.core.common.result.AppError
import com.example.core.common.result.AppResult
import com.example.core.common.text.UiText
import com.example.core.data.model.PlateLookupInfo
import com.example.core.data.model.PlateLookupResult
import com.example.core.data.model.Vehicle
import com.example.core.data.model.VehicleType
import com.example.core.data.repository.PlateLookupRepository
import com.example.core.data.repository.VehicleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleFormViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var vehicleRepository: VehicleRepository
  private lateinit var plateLookupRepository: PlateLookupRepository

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    vehicleRepository = mockk(relaxed = true)
    plateLookupRepository = mockk(relaxed = true)
    every { vehicleRepository.observeVehicles() } returns flowOf(emptyList())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `validates legacy and Mercosul plates in real time and detects invalid pattern`() =
    runTest(testDispatcher) {
      coEvery { plateLookupRepository.lookupByPlate(any()) } returns
        AppResult.Success(PlateLookupResult.NotFound)

      val viewModel =
        VehicleFormViewModel(SavedStateHandle(), vehicleRepository, plateLookupRepository)
      advanceUntilIdle()

      viewModel.onPlateChanged("abc1234")
      assertEquals("ABC1234", viewModel.uiState.value.plate)
      assertEquals(PlatePattern.LEGACY, viewModel.uiState.value.platePattern)
      assertNull(viewModel.uiState.value.plateError)

      viewModel.onPlateChanged("bra2e19")
      assertEquals("BRA2E19", viewModel.uiState.value.plate)
      assertEquals(PlatePattern.MERCOSUL, viewModel.uiState.value.platePattern)
      assertNull(viewModel.uiState.value.plateError)

      viewModel.onPlateChanged("1234567")
      assertEquals(PlatePattern.INVALID, viewModel.uiState.value.platePattern)
      assertNotNull(viewModel.uiState.value.plateError)
    }

  @Test
  fun `automatic plate lookup handles Found, NotFound, and Error gracefully`() =
    runTest(testDispatcher) {
      val info =
        PlateLookupInfo(
          plate = "BRA2E19",
          type = VehicleType.CAR,
          brand = "Hyundai",
          model = "HB20",
          year = 2023,
          color = "Prata",
        )
      coEvery { plateLookupRepository.lookupByPlate("BRA2E19") } returns
        AppResult.Success(PlateLookupResult.Found(info))
      coEvery { plateLookupRepository.lookupByPlate("ZZZ9999") } returns
        AppResult.Success(PlateLookupResult.NotFound)
      coEvery { plateLookupRepository.lookupByPlate("ERR1234") } returns
        AppResult.Error(AppError.ServerUnavailable)

      val viewModel =
        VehicleFormViewModel(SavedStateHandle(), vehicleRepository, plateLookupRepository)
      advanceUntilIdle()

      // Found
      viewModel.onPlateChanged("BRA2E19")
      advanceUntilIdle()
      assertEquals(PlateLookupStatus.Found, viewModel.uiState.value.lookupStatus)
      assertEquals("Hyundai", viewModel.uiState.value.brand)
      assertEquals("HB20", viewModel.uiState.value.model)
      assertEquals(2023, viewModel.uiState.value.year)

      // NotFound
      viewModel.onPlateChanged("ZZZ9999")
      advanceUntilIdle()
      assertEquals(PlateLookupStatus.NotFound, viewModel.uiState.value.lookupStatus)

      // Error
      viewModel.onPlateChanged("ERR1234")
      advanceUntilIdle()
      assertTrue(viewModel.uiState.value.lookupStatus is PlateLookupStatus.Error)
    }

  @Test
  fun `duplicate plate error attaches to plate field and focuses plate`() =
    runTest(testDispatcher) {
      coEvery { plateLookupRepository.lookupByPlate("BRA2E19") } returns
        AppResult.Success(PlateLookupResult.NotFound)
      coEvery { vehicleRepository.addVehicle(any()) } returns
        AppResult.Error(AppError.Validation(UiText.DynamicString("Placa duplicada")))

      val viewModel =
        VehicleFormViewModel(SavedStateHandle(), vehicleRepository, plateLookupRepository)
      advanceUntilIdle()

      viewModel.onPlateChanged("BRA2E19")
      advanceUntilIdle()
      viewModel.onBrandSelected("Hyundai")
      viewModel.onModelSelected("HB20")
      viewModel.onYearSelected(2023)

      viewModel.events.test {
        viewModel.saveClick()
        advanceUntilIdle()

        val focusEvent = awaitItem()
        assertEquals(VehicleFormEvent.FocusField(VehicleFormFocusTarget.PLATE), focusEvent)
        assertNotNull(viewModel.uiState.value.plateError)
        assertFalse(viewModel.uiState.value.isSaving)
      }
    }

  @Test
  fun `double tap on saveClick only invokes repository once while saving`() =
    runTest(testDispatcher) {
      val saveGate = CompletableDeferred<AppResult<Vehicle>>()
      coEvery { plateLookupRepository.lookupByPlate(any()) } returns
        AppResult.Success(PlateLookupResult.NotFound)
      coEvery { vehicleRepository.addVehicle(any()) } coAnswers { saveGate.await() }

      val viewModel =
        VehicleFormViewModel(SavedStateHandle(), vehicleRepository, plateLookupRepository)
      advanceUntilIdle()

      viewModel.onPlateChanged("BRA2E19")
      advanceUntilIdle()
      viewModel.onBrandSelected("Hyundai")
      viewModel.onModelSelected("HB20")
      viewModel.onYearSelected(2023)

      viewModel.saveClick()
      viewModel.saveClick()
      testScheduler.runCurrent()

      assertTrue(viewModel.uiState.value.isSaving)
      coVerify(exactly = 1) { vehicleRepository.addVehicle(any()) }

      saveGate.complete(
        AppResult.Success(
          Vehicle(
            id = "v1",
            plate = "BRA2E19",
            type = VehicleType.CAR,
            brand = "Hyundai",
            model = "HB20",
            year = 2023,
            isPrimary = true,
          ),
        ),
      )
      advanceUntilIdle()
      assertFalse(viewModel.uiState.value.isSaving)
    }
}
