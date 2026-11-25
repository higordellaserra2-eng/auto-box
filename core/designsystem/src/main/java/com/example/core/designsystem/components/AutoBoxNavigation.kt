package com.example.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.R
import com.example.core.designsystem.preview.AutoBoxComponentPreviews
import com.example.core.designsystem.theme.AutoBoxSpacing
import com.example.core.designsystem.theme.AutoBoxTheme

data class AutoBoxNavDestination(
  val routeId: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoBoxTopAppBar(
  title: String,
  modifier: Modifier = Modifier,
  canNavigateBack: Boolean = false,
  onNavigateBack: () -> Unit = {},
  actions: @Composable RowScope.() -> Unit = {},
  scrollBehavior: TopAppBarScrollBehavior? = null,
) {
  CenterAlignedTopAppBar(
    title = {
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.semantics { heading() },
      )
    },
    navigationIcon = {
      if (canNavigateBack) {
        IconButton(
          onClick = onNavigateBack,
          modifier =
            Modifier
              .minimumInteractiveComponentSize()
              .testTag("top_bar_back_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(R.string.cd_navigate_back),
          )
        }
      }
    },
    actions = actions,
    colors =
      TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
      ),
    scrollBehavior = scrollBehavior,
    modifier = modifier,
  )
}

@Composable
fun AutoBoxAdaptiveScaffold(
  widthSizeClass: WindowWidthSizeClass,
  destinations: List<AutoBoxNavDestination>,
  selectedRouteId: String,
  onSelectDestination: (AutoBoxNavDestination) -> Unit,
  modifier: Modifier = Modifier,
  topBar: @Composable () -> Unit = {},
  snackbarHostState: SnackbarHostState = SnackbarHostState(),
  floatingActionButton: @Composable () -> Unit = {},
  content: @Composable (PaddingValues) -> Unit,
) {
  val useRail = widthSizeClass != WindowWidthSizeClass.Compact

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = topBar,
    snackbarHost = { AutoBoxSnackbarHost(hostState = snackbarHostState) },
    floatingActionButton = floatingActionButton,
    bottomBar = {
      if (!useRail && destinations.isNotEmpty()) {
        NavigationBar(
          windowInsets = WindowInsets.navigationBars,
          containerColor = MaterialTheme.colorScheme.surface,
        ) {
          destinations.forEach { destination ->
            val selected = destination.routeId == selectedRouteId
            NavigationBarItem(
              selected = selected,
              onClick = { onSelectDestination(destination) },
              icon = {
                Icon(
                  imageVector =
                    if (selected) destination.selectedIcon else destination.unselectedIcon,
                  contentDescription = null,
                )
              },
              label = {
                Text(
                  text = destination.label,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              },
              modifier = Modifier.testTag(destination.testTag),
            )
          }
        }
      }
    },
  ) { innerPadding ->
    if (useRail && destinations.isNotEmpty()) {
      Row(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(innerPadding),
      ) {
        NavigationRail(
          modifier = Modifier.fillMaxHeight(),
          containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
          Spacer(modifier = Modifier.weight(0.1f))
          destinations.forEach { destination ->
            val selected = destination.routeId == selectedRouteId
            NavigationRailItem(
              selected = selected,
              onClick = { onSelectDestination(destination) },
              icon = {
                Icon(
                  imageVector =
                    if (selected) destination.selectedIcon else destination.unselectedIcon,
                  contentDescription = null,
                )
              },
              label = {
                Text(
                  text = destination.label,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              },
              modifier = Modifier.testTag(destination.testTag),
            )
          }
          Spacer(modifier = Modifier.weight(1f))
        }
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.TopCenter,
        ) {
          val maxWidth =
            if (widthSizeClass == WindowWidthSizeClass.Expanded) 1120.dp else 840.dp
          Box(modifier = Modifier.widthIn(max = maxWidth).fillMaxSize()) {
            content(PaddingValues(0.dp))
          }
        }
      }
    } else {
      content(innerPadding)
    }
  }
}

class AutoBoxSnackbarVisuals(
  override val message: String,
  val tone: AutoBoxStatusTone = AutoBoxStatusTone.INFO,
  override val actionLabel: String? = null,
  override val withDismissAction: Boolean = true,
  override val duration: SnackbarDuration = SnackbarDuration.Short,
) : SnackbarVisuals

@Composable
fun AutoBoxSnackbarHost(
  hostState: SnackbarHostState,
  modifier: Modifier = Modifier,
) {
  SnackbarHost(
    hostState = hostState,
    modifier = modifier.padding(AutoBoxSpacing.sm),
  ) { data ->
    AutoBoxSnackbar(data = data)
  }
}

@Composable
fun AutoBoxSnackbar(
  data: SnackbarData,
  modifier: Modifier = Modifier,
) {
  val tone = (data.visuals as? AutoBoxSnackbarVisuals)?.tone ?: AutoBoxStatusTone.INFO
  val semanticColors = AutoBoxTheme.semanticColors
  val (containerColor, contentColor, icon) =
    when (tone) {
      AutoBoxStatusTone.SUCCESS ->
        Triple(
          semanticColors.successContainer,
          semanticColors.onSuccessContainer,
          Icons.Rounded.CheckCircle,
        )
      AutoBoxStatusTone.ERROR ->
        Triple(
          MaterialTheme.colorScheme.errorContainer,
          MaterialTheme.colorScheme.onErrorContainer,
          Icons.Rounded.ErrorOutline,
        )
      AutoBoxStatusTone.WARNING ->
        Triple(
          semanticColors.warningContainer,
          semanticColors.onWarningContainer,
          Icons.Rounded.Info,
        )
      AutoBoxStatusTone.INFO,
      AutoBoxStatusTone.NEUTRAL ->
        Triple(
          MaterialTheme.colorScheme.inverseSurface,
          MaterialTheme.colorScheme.inverseOnSurface,
          Icons.Rounded.Info,
        )
    }

  Snackbar(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    containerColor = containerColor,
    contentColor = contentColor,
    action =
      data.visuals.actionLabel?.let { label ->
        {
          TextButton(
            onClick = { data.performAction() },
            modifier = Modifier.minimumInteractiveComponentSize(),
          ) {
            Text(text = label, color = contentColor)
          }
        }
      },
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(20.dp),
      )
      Spacer(modifier = Modifier.width(AutoBoxSpacing.xs))
      Text(
        text = data.visuals.message,
        style = MaterialTheme.typography.bodyMedium,
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@AutoBoxComponentPreviews
@Composable
private fun AutoBoxNavigationPreview() {
  val destinations =
    listOf(
      AutoBoxNavDestination(
        routeId = "home",
        label = "Início",
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_home",
      ),
      AutoBoxNavDestination(
        routeId = "vehicles",
        label = "Veículos",
        selectedIcon = Icons.Rounded.DirectionsCar,
        unselectedIcon = Icons.Outlined.DirectionsCar,
        testTag = "nav_vehicles",
      ),
    )

  AutoBoxTheme {
    AutoBoxAdaptiveScaffold(
      widthSizeClass = WindowWidthSizeClass.Compact,
      destinations = destinations,
      selectedRouteId = "home",
      onSelectDestination = {},
      topBar = { AutoBoxTopAppBar(title = "AutoBox") },
    ) { padding ->
      Column(modifier = Modifier.padding(padding).padding(AutoBoxSpacing.md)) {
        Text(text = "Conteúdo principal", style = MaterialTheme.typography.bodyLarge)
      }
    }
  }
}
