package com.example.feature.home

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.example.core.common.text.UiText
import com.example.core.data.model.HomeSummary
import com.example.core.data.model.PointsSummary
import com.example.core.data.model.ReminderPriority
import com.example.core.data.model.ServiceReminder
import com.example.core.data.model.VehicleSummary
import com.example.core.designsystem.theme.AutoBoxTheme
import java.time.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HomeScreenTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `success state renders headings, accessible shortcuts, and offline banner`() {
    var payClicked = false
    var vehicleClicked = false

    val summary =
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
            title = "Troca de óleo e filtro",
            description = "Manutenção preventiva",
            vehicleLabel = "HB20 · BRA2E19",
            dueDate = LocalDate.of(2026, 12, 7),
            priority = ReminderPriority.DUE_SOON,
          ),
        pendingPaymentAmountCents = 18990L,
      )

    composeTestRule.setContent {
      AutoBoxTheme {
        HomeScreen(
          uiState = HomeUiState.Success(summary = summary),
          isOffline = true,
          onRefresh = {},
          onRetry = {},
          onPayClick = { payClicked = true },
          onRegisterVehicleClick = { vehicleClicked = true },
          onOpenRewardsClick = {},
        )
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("offline_banner").assertIsDisplayed()
    composeTestRule
      .onNodeWithText("Olá, Mariana")
      .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
      .assertIsDisplayed()

    composeTestRule
      .onNodeWithTag("home_success_list")
      .performScrollToNode(hasTestTag("home_pay_shortcut"))
    composeTestRule.onNodeWithTag("home_pay_shortcut").assertHasClickAction().performClick()

    composeTestRule
      .onNodeWithTag("home_success_list")
      .performScrollToNode(hasTestTag("home_register_vehicle_shortcut"))
    composeTestRule
      .onNodeWithTag("home_register_vehicle_shortcut")
      .assertHasClickAction()
      .performClick()

    assertTrue(payClicked)
    assertTrue(vehicleClicked)
  }

  @Test
  fun `error state announces live region and invokes retry`() {
    var retryClicked = false

    composeTestRule.setContent {
      AutoBoxTheme {
        HomeScreen(
          uiState = HomeUiState.Error(UiText.DynamicString("Sem conexão no momento.")),
          isOffline = true,
          onRefresh = {},
          onRetry = { retryClicked = true },
          onPayClick = {},
          onRegisterVehicleClick = {},
          onOpenRewardsClick = {},
        )
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule
      .onNode(hasTestTag("error_state"))
      .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion))
      .assertIsDisplayed()

    composeTestRule.onNodeWithTag("error_state_retry_button").performClick()
    assertTrue(retryClicked)
  }

  @Test
  fun `success state without primary vehicle displays empty vehicle prompt and triggers registration`() {
    var vehicleClicked = false

    val summary =
      HomeSummary(
        customerFirstName = "Mariana",
        pointsSummary =
          PointsSummary(
            currentPoints = 720,
            nextRewardPoints = 1000,
            nextRewardTitle = "Lavagem completa grátis",
            cashbackAvailableCents = 2500L,
          ),
        primaryVehicle = null,
        nextReminder = null,
        pendingPaymentAmountCents = null,
      )

    composeTestRule.setContent {
      AutoBoxTheme {
        HomeScreen(
          uiState = HomeUiState.Success(summary = summary),
          isOffline = false,
          onRefresh = {},
          onRetry = {},
          onPayClick = {},
          onRegisterVehicleClick = { vehicleClicked = true },
          onOpenRewardsClick = {},
        )
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("home_empty_vehicle_card").assertIsDisplayed().performClick()
    assertTrue(vehicleClicked)
  }
}
