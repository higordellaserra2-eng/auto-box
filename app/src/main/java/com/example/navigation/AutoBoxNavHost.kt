package com.example.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.example.R
import com.example.core.designsystem.components.AutoBoxAdaptiveScaffold
import com.example.core.designsystem.components.AutoBoxNavDestination
import com.example.core.designsystem.components.AutoBoxTopAppBar
import com.example.feature.home.HomeRoute
import com.example.feature.onboarding.OnboardingRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoBoxAppShell(
  widthSizeClass: WindowWidthSizeClass,
  startOnOnboarding: Boolean,
  navController: NavHostController = rememberNavController(),
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = backStackEntry?.destination

  val isOnboarding = currentDestination?.hasRoute<OnboardingDestination>() == true
  val isPayment = currentDestination?.hasRoute<PaymentDestination>() == true

  val selectedTopLevel =
    TopLevelDestination.entries.firstOrNull { top ->
      currentDestination?.hasRoute(top.routeObject::class) == true
    } ?: TopLevelDestination.HOME

  val navItems =
    TopLevelDestination.entries.map { dest ->
      AutoBoxNavDestination(
        routeId = dest.routeId,
        label = stringResource(dest.labelRes),
        selectedIcon = dest.selectedIcon,
        unselectedIcon = dest.unselectedIcon,
        testTag = dest.testTag,
      )
    }

  val startDestination: Any =
    if (startOnOnboarding) OnboardingDestination else HomeDestination

  if (isOnboarding) {
    NavHost(
      navController = navController,
      startDestination = startDestination,
    ) {
      composable<OnboardingDestination> {
        OnboardingRoute(
          onNavigateToHome = {
            navController.navigate(HomeDestination) {
              popUpTo<OnboardingDestination> { inclusive = true }
            }
          },
        )
      }
      composable<HomeDestination> {}
    }
    return
  }

  AutoBoxAdaptiveScaffold(
    widthSizeClass = widthSizeClass,
    destinations = if (isPayment) emptyList() else navItems,
    selectedRouteId = selectedTopLevel.routeId,
    snackbarHostState = snackbarHostState,
    onSelectDestination = { item ->
      val target = TopLevelDestination.entries.first { it.routeId == item.routeId }
      navController.navigate(target.routeObject) {
        popUpTo(navController.graph.findStartDestination().id) {
          saveState = true
        }
        launchSingleTop = true
        restoreState = true
      }
    },
    topBar = {
      val title =
        if (isPayment) {
          stringResource(R.string.stub_payment_title)
        } else {
          when (selectedTopLevel) {
            TopLevelDestination.HOME -> stringResource(R.string.app_name)
            else -> stringResource(selectedTopLevel.labelRes)
          }
        }
      AutoBoxTopAppBar(
        title = title,
        canNavigateBack = isPayment,
        onNavigateBack = { navController.popBackStack() },
      )
    },
  ) { innerPadding ->
    NavHost(
      navController = navController,
      startDestination = startDestination,
      modifier = Modifier.padding(innerPadding),
    ) {
      composable<OnboardingDestination> {
        OnboardingRoute(
          onNavigateToHome = {
            navController.navigate(HomeDestination) {
              popUpTo<OnboardingDestination> { inclusive = true }
            }
          },
        )
      }

      composable<HomeDestination>(
        deepLinks = listOf(navDeepLink<HomeDestination>(basePath = AutoBoxDeepLinks.HOME)),
      ) {
        HomeRoute(
          onPayClick = { navController.navigate(PaymentDestination()) },
          onRegisterVehicleClick = {
            navController.navigate(VehiclesDestination) {
              popUpTo(navController.graph.findStartDestination().id) { saveState = true }
              launchSingleTop = true
              restoreState = true
            }
          },
          onOpenRewardsClick = {
            navController.navigate(RewardsDestination) {
              popUpTo(navController.graph.findStartDestination().id) { saveState = true }
              launchSingleTop = true
              restoreState = true
            }
          },
        )
      }

      composable<HistoryDestination>(
        deepLinks = listOf(navDeepLink<HistoryDestination>(basePath = AutoBoxDeepLinks.HISTORY)),
      ) {
        FeatureExtensionPlaceholderScreen(
          title = stringResource(R.string.stub_history_title),
          description = stringResource(R.string.stub_history_desc),
          icon = ExtensionIcons.history,
        )
      }

      composable<RewardsDestination>(
        deepLinks = listOf(navDeepLink<RewardsDestination>(basePath = AutoBoxDeepLinks.REWARDS)),
      ) {
        FeatureExtensionPlaceholderScreen(
          title = stringResource(R.string.stub_rewards_title),
          description = stringResource(R.string.stub_rewards_desc),
          icon = ExtensionIcons.rewards,
        )
      }

      composable<VehiclesDestination>(
        deepLinks = listOf(navDeepLink<VehiclesDestination>(basePath = AutoBoxDeepLinks.VEHICLES)),
      ) {
        FeatureExtensionPlaceholderScreen(
          title = stringResource(R.string.stub_vehicles_title),
          description = stringResource(R.string.stub_vehicles_desc),
          icon = ExtensionIcons.vehicles,
        )
      }

      composable<ProfileDestination>(
        deepLinks = listOf(navDeepLink<ProfileDestination>(basePath = AutoBoxDeepLinks.PROFILE)),
      ) {
        FeatureExtensionPlaceholderScreen(
          title = stringResource(R.string.stub_profile_title),
          description = stringResource(R.string.stub_profile_desc),
          icon = ExtensionIcons.profile,
        )
      }

      composable<PaymentDestination>(
        deepLinks = listOf(navDeepLink<PaymentDestination>(basePath = AutoBoxDeepLinks.PAYMENT)),
      ) {
        FeatureExtensionPlaceholderScreen(
          title = stringResource(R.string.stub_payment_title),
          description = stringResource(R.string.stub_payment_desc),
          icon = ExtensionIcons.payment,
          actionLabel = stringResource(R.string.stub_action_back_home),
          onAction = { navController.popBackStack() },
        )
      }
    }
  }
}
