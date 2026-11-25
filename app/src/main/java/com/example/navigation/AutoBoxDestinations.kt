package com.example.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R
import kotlinx.serialization.Serializable

object AutoBoxDeepLinks {
  const val BASE_URI = "autobox://app"
  const val HOME = "$BASE_URI/home"
  const val HISTORY = "$BASE_URI/history"
  const val REWARDS = "$BASE_URI/rewards"
  const val VEHICLES = "$BASE_URI/vehicles"
  const val PROFILE = "$BASE_URI/profile"
  const val PAYMENT = "$BASE_URI/payment"
}

@Serializable
data object OnboardingDestination

@Serializable
data object HomeDestination

@Serializable
data object HistoryDestination

@Serializable
data object RewardsDestination

@Serializable
data object VehiclesDestination

@Serializable
data object ProfileDestination

@Serializable
data class PaymentDestination(val orderId: String? = null)

enum class TopLevelDestination(
  val routeId: String,
  @StringRes val labelRes: Int,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
  val routeObject: Any,
) {
  HOME(
    routeId = "home",
    labelRes = R.string.nav_home,
    selectedIcon = Icons.Rounded.Home,
    unselectedIcon = Icons.Outlined.Home,
    testTag = "bottom_nav_home",
    routeObject = HomeDestination,
  ),
  HISTORY(
    routeId = "history",
    labelRes = R.string.nav_history,
    selectedIcon = Icons.Rounded.History,
    unselectedIcon = Icons.Outlined.History,
    testTag = "bottom_nav_history",
    routeObject = HistoryDestination,
  ),
  REWARDS(
    routeId = "rewards",
    labelRes = R.string.nav_rewards,
    selectedIcon = Icons.Rounded.Stars,
    unselectedIcon = Icons.Outlined.Stars,
    testTag = "bottom_nav_rewards",
    routeObject = RewardsDestination,
  ),
  VEHICLES(
    routeId = "vehicles",
    labelRes = R.string.nav_vehicles,
    selectedIcon = Icons.Rounded.DirectionsCar,
    unselectedIcon = Icons.Outlined.DirectionsCar,
    testTag = "bottom_nav_vehicles",
    routeObject = VehiclesDestination,
  ),
  PROFILE(
    routeId = "profile",
    labelRes = R.string.nav_profile,
    selectedIcon = Icons.Rounded.Person,
    unselectedIcon = Icons.Outlined.Person,
    testTag = "bottom_nav_profile",
    routeObject = ProfileDestination,
  ),
}
