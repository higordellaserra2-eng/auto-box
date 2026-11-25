package com.example.feature.vehicles

import androidx.compose.material3.Surface
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import com.example.core.common.result.AppResult
import com.example.core.data.model.PlateLookupInfo
import com.example.core.data.model.PlateLookupResult
import com.example.core.data.model.Vehicle
import com.example.core.data.model.VehicleType
import com.example.core.data.repository.PlateLookupRepository
import com.example.core.data.repository.VehicleRepository
import com.example.core.designsystem.theme.AutoBoxTheme
import com.example.feature.vehicles.form.VehicleFormRoute
import com.example.feature.vehicles.form.VehicleFormViewModel
import com.example.feature.vehicles.list.VehicleListScreen
import com.example.feature.vehicles.list.VehicleListUiState
import com.example.feature.vehicles.picker.VehiclePickerSheetContent
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VehicleScreensTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `empty vehicle list displays empty CTA and triggers navigation`() {
    var addClicked = false

    composeTestRule.setContent {
      AutoBoxTheme {
        VehicleListScreen(
          uiState = VehicleListUiState.Empty,
          isOffline = false,
          onAddVehicleClick = { addClicked = true },
          onEditVehicleClick = {},
          onSetPrimaryClick = {},
          onRequestDeleteClick = {},
          onConfirmDelete = {},
          onDismissDeleteDialog = {},
          onRetry = {},
        )
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Nenhum veículo cadastrado").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cadastre seu primeiro veículo").assertIsDisplayed()
    composeTestRule.onNodeWithTag("empty_state_cta").performClick()

    assertTrue(addClicked)
  }

  @Test
  fun `vehicle card exposes TalkBack contentDescription and customActions`() {
    var editClicked = false
    var primaryClicked = false
    var deleteClicked = false

    val regularVehicle =
      Vehicle(
        id = "v2",
        plate = "ABC1234",
        type = VehicleType.CAR,
        brand = "Chevrolet",
        model = "Onix",
        year = 2022,
        nickname = "Carro de trabalho",
        isPrimary = false,
      )

    composeTestRule.setContent {
      AutoBoxTheme {
        VehicleListScreen(
          uiState = VehicleListUiState.Success(listOf(regularVehicle)),
          isOffline = false,
          onAddVehicleClick = {},
          onEditVehicleClick = { editClicked = true },
          onSetPrimaryClick = { primaryClicked = true },
          onRequestDeleteClick = { deleteClicked = true },
          onConfirmDelete = {},
          onDismissDeleteDialog = {},
          onRetry = {},
        )
      }
    }
    composeTestRule.waitForIdle()

    val cardNode = composeTestRule.onNodeWithTag("vehicle_card_v2")
    cardNode.assertIsDisplayed()
    cardNode.assert(
      SemanticsMatcher("Has plate and model in contentDescription") { node ->
        val cd = node.config.getOrElseNullable(SemanticsProperties.ContentDescription) { null }
          ?.joinToString(" ")
          .orEmpty()
        cd.contains("Chevrolet") && cd.contains("Onix") && cd.contains("A B C 1 2 3 4")
      },
    )
    cardNode.assert(
      SemanticsMatcher("Has Editar, Definir como principal, and Excluir customActions") { node ->
        val actions = node.config.getOrElseNullable(SemanticsActions.CustomActions) { null }.orEmpty()
        val labels = actions.map { it.label }
        labels.containsAll(listOf("Editar", "Definir como principal", "Excluir"))
      },
    )

    val actions =
      cardNode.fetchSemanticsNode().config[SemanticsActions.CustomActions]
    actions.first { it.label == "Editar" }.action()
    actions.first { it.label == "Definir como principal" }.action()
    actions.first { it.label == "Excluir" }.action()

    assertTrue(editClicked)
    assertTrue(primaryClicked)
    assertTrue(deleteClicked)
  }

  @Test
  fun `complete registration flow auto-fills from plate lookup and saves vehicle`() {
    val vehicleRepository = mockk<VehicleRepository>()
    every { vehicleRepository.observeVehicles() } returns flowOf(emptyList())
    coEvery { vehicleRepository.addVehicle(any()) } answers {
      val input = firstArg<Vehicle>()
      AppResult.Success(input.copy(id = "saved-1", isPrimary = true))
    }

    val lookupRepository = mockk<PlateLookupRepository>()
    coEvery { lookupRepository.lookupByPlate("BRA2E19") } returns
      AppResult.Success(
        PlateLookupResult.Found(
          PlateLookupInfo(
            plate = "BRA2E19",
            type = VehicleType.CAR,
            brand = "Hyundai",
            model = "HB20",
            year = 2023,
            color = "Prata",
          ),
        ),
      )

    val viewModel =
      VehicleFormViewModel(SavedStateHandle(), vehicleRepository, lookupRepository)
    var savedMessage: String? = null

    composeTestRule.setContent {
      AutoBoxTheme {
        VehicleFormRoute(
          vehicleId = null,
          onSaved = { savedMessage = it },
          onNavigateBack = {},
          viewModel = viewModel,
        )
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("vehicle_plate_input").performTextInput("BRA2E19")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("vehicle_plate_pattern_badge").assertIsDisplayed()
    composeTestRule.onNodeWithTag("vehicle_lookup_found").assertIsDisplayed()

    composeTestRule
      .onNodeWithTag("vehicle_form_save_button")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()

    assertEquals("Veículo BRA2E19 cadastrado com sucesso.", savedMessage)
  }

  @Test
  fun `invalid plate shows error message and blocks save`() {
    val vehicleRepository = mockk<VehicleRepository>()
    every { vehicleRepository.observeVehicles() } returns flowOf(emptyList())
    val lookupRepository = mockk<PlateLookupRepository>()

    val viewModel =
      VehicleFormViewModel(SavedStateHandle(), vehicleRepository, lookupRepository)

    composeTestRule.setContent {
      AutoBoxTheme {
        VehicleFormRoute(
          vehicleId = null,
          onSaved = {},
          onNavigateBack = {},
          viewModel = viewModel,
        )
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("vehicle_plate_input").performTextInput("1234567")
    composeTestRule.waitForIdle()

    composeTestRule
      .onNodeWithText("Placa inválida. Use o formato AAA-9999 ou AAA9A99.")
      .assertIsDisplayed()
  }

  @Test
  fun `vehicle picker sheet selects vehicle and supports add new CTA`() {
    var selectedVehicle: Vehicle? = null
    var addNewClicked = false

    val vehicles =
      listOf(
        Vehicle(
          id = "1",
          plate = "BRA2E19",
          type = VehicleType.CAR,
          brand = "Hyundai",
          model = "HB20",
          year = 2023,
          isPrimary = true,
        ),
        Vehicle(
          id = "2",
          plate = "ABC1234",
          type = VehicleType.CAR,
          brand = "Chevrolet",
          model = "Onix",
          year = 2022,
          isPrimary = false,
        ),
      )

    composeTestRule.setContent {
      AutoBoxTheme {
        Surface {
          VehiclePickerSheetContent(
            vehicles = vehicles,
            selectedVehicleId = "1",
            onSelectVehicle = { selectedVehicle = it },
            onAddNewVehicle = { addNewClicked = true },
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("vehicle_picker_item_2").performClick()
    assertEquals("2", selectedVehicle?.id)

    composeTestRule.onNodeWithTag("vehicle_picker_add_new_button").performClick()
    assertTrue(addNewClicked)
  }
}
