package com.example.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.common.format.BrFormatters
import com.example.core.data.model.HomeSummary
import com.example.core.data.model.PointsSummary
import com.example.core.data.model.ReminderPriority
import com.example.core.data.model.ServiceReminder
import com.example.core.data.model.VehicleSummary
import com.example.core.designsystem.components.AutoBoxButton
import com.example.core.designsystem.components.AutoBoxButtonVariant
import com.example.core.designsystem.components.AutoBoxCard
import com.example.core.designsystem.components.AutoBoxCardVariant
import com.example.core.designsystem.components.AutoBoxStatusChip
import com.example.core.designsystem.components.AutoBoxStatusTone
import com.example.core.designsystem.components.EmptyState
import com.example.core.designsystem.components.ErrorState
import com.example.core.designsystem.components.LoadingState
import com.example.core.designsystem.components.OfflineBanner
import com.example.core.designsystem.preview.AutoBoxComponentPreviews
import com.example.core.designsystem.theme.AutoBoxMonoTypography
import com.example.core.designsystem.theme.AutoBoxSpacing
import com.example.core.designsystem.theme.AutoBoxTheme
import java.time.LocalDate

@Composable
fun HomeRoute(
  onPayClick: () -> Unit,
  onRegisterVehicleClick: () -> Unit,
  onOpenRewardsClick: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: HomeViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()

  HomeScreen(
    uiState = uiState,
    isOffline = isOffline,
    onRefresh = viewModel::refresh,
    onRetry = viewModel::retry,
    onPayClick = onPayClick,
    onRegisterVehicleClick = onRegisterVehicleClick,
    onOpenRewardsClick = onOpenRewardsClick,
    modifier = modifier,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  uiState: HomeUiState,
  isOffline: Boolean,
  onRefresh: () -> Unit,
  onRetry: () -> Unit,
  onPayClick: () -> Unit,
  onRegisterVehicleClick: () -> Unit,
  onOpenRewardsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxSize()) {
    OfflineBanner(isOffline = isOffline)

    PullToRefreshBox(
      isRefreshing = (uiState as? HomeUiState.Success)?.isRefreshing == true,
      onRefresh = onRefresh,
      modifier = Modifier.fillMaxSize(),
    ) {
      when (uiState) {
        HomeUiState.Loading -> {
          LoadingState(
            skeletonCount = 3,
            modifier = Modifier.fillMaxSize(),
          )
        }

        HomeUiState.Empty -> {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
          ) {
            EmptyState(
              title = stringResource(R.string.home_empty_title),
              description = stringResource(R.string.home_empty_description),
              icon = Icons.Rounded.DirectionsCar,
              actionLabel = stringResource(R.string.home_action_register_vehicle),
              onAction = onRegisterVehicleClick,
            )
          }
        }

        is HomeUiState.Error -> {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
          ) {
            ErrorState(
              title = stringResource(R.string.home_error_title),
              message = uiState.message.asString(),
              onRetry = onRetry,
            )
          }
        }

        is HomeUiState.Success -> {
          HomeSuccessContent(
            summary = uiState.summary,
            onPayClick = onPayClick,
            onRegisterVehicleClick = onRegisterVehicleClick,
            onOpenRewardsClick = onOpenRewardsClick,
          )
        }
      }
    }
  }
}

@Composable
private fun HomeSuccessContent(
  summary: HomeSummary,
  onPayClick: () -> Unit,
  onRegisterVehicleClick: () -> Unit,
  onOpenRewardsClick: () -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().testTag("home_success_list"),
    contentPadding =
      PaddingValues(
        horizontal = AutoBoxSpacing.md,
        vertical = AutoBoxSpacing.md,
      ),
    verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.md),
  ) {
    item(key = "header") {
      HomeHeaderSection(
        firstName = summary.customerFirstName,
        primaryVehicle = summary.primaryVehicle,
      )
    }

    item(key = "points_hero") {
      PointsBalanceHeroCard(
        pointsSummary = summary.pointsSummary,
        onClick = onOpenRewardsClick,
      )
    }

    item(key = "quick_actions") {
      QuickActionsSection(
        pendingPaymentAmountCents = summary.pendingPaymentAmountCents,
        onPayClick = onPayClick,
        onRegisterVehicleClick = onRegisterVehicleClick,
      )
    }

    summary.nextReminder?.let { reminder ->
      item(key = "next_reminder_${reminder.id}") {
        NextReminderSection(reminder = reminder)
      }
    }
  }
}

@Composable
private fun HomeHeaderSection(
  firstName: String,
  primaryVehicle: VehicleSummary?,
) {
  Column(verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs)) {
    Text(
      text = stringResource(R.string.home_greeting, firstName),
      style = MaterialTheme.typography.headlineMedium,
      modifier = Modifier.semantics { heading() },
    )
    Text(
      text = stringResource(R.string.home_subtitle),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (primaryVehicle != null) {
      Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.padding(top = AutoBoxSpacing.xxs),
      ) {
        Row(
          modifier =
            Modifier.padding(
              horizontal = AutoBoxSpacing.sm,
              vertical = AutoBoxSpacing.xs,
            ),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs),
        ) {
          Icon(
            imageVector = Icons.Rounded.DirectionsCar,
            contentDescription = stringResource(R.string.home_primary_vehicle_label),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = "${primaryVehicle.model} (${primaryVehicle.year})",
            style = MaterialTheme.typography.bodyMedium,
          )
          Spacer(modifier = Modifier.weight(1f))
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          ) {
            Text(
              text = BrFormatters.formatPlate(primaryVehicle.plate),
              style = AutoBoxMonoTypography.plate,
              modifier = Modifier.padding(horizontal = AutoBoxSpacing.xs, vertical = 2.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PointsBalanceHeroCard(
  pointsSummary: PointsSummary,
  onClick: () -> Unit,
) {
  val pointsText =
    pluralStringResource(
      id = R.plurals.home_points_balance,
      count = pointsSummary.currentPoints,
      BrFormatters.formatPoints(pointsSummary.currentPoints),
    )
  val remainingPoints = (pointsSummary.nextRewardPoints - pointsSummary.currentPoints).coerceAtLeast(0)
  val progressCd =
    stringResource(
      R.string.home_points_progress_cd,
      pointsSummary.currentPoints,
      pointsSummary.nextRewardPoints,
      pointsSummary.nextRewardTitle,
    )

  AutoBoxCard(
    onClick = onClick,
    variant = AutoBoxCardVariant.ELEVATED,
    contentPadding = PaddingValues(0.dp),
    testTag = "home_points_card",
    modifier = Modifier.fillMaxWidth(),
  ) {
    val gradient =
      Brush.linearGradient(
        colors =
          listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.86f),
          ),
      )
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .background(gradient)
          .padding(AutoBoxSpacing.md),
      verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.sm),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs),
      ) {
        Icon(
          imageVector = Icons.Rounded.Stars,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onPrimary,
          modifier = Modifier.size(22.dp),
        )
        Text(
          text = stringResource(R.string.home_points_section_title),
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onPrimary,
          modifier = Modifier.semantics { heading() },
        )
      }

      Text(
        text = pointsText,
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.onPrimary,
      )

      Text(
        text = stringResource(R.string.home_points_next_reward, pointsSummary.nextRewardTitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f),
      )

      LinearProgressIndicator(
        progress = { pointsSummary.progressFraction },
        color = MaterialTheme.colorScheme.secondaryContainer,
        trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
        modifier =
          Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(999.dp))
            .semantics {
              contentDescription = progressCd
              progressBarRangeInfo = ProgressBarRangeInfo(pointsSummary.progressFraction, 0f..1f)
            },
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text =
            stringResource(
              R.string.home_points_remaining,
              BrFormatters.formatPoints(remainingPoints),
            ),
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
        )
        Text(
          text =
            stringResource(
              R.string.home_cashback_label,
              BrFormatters.formatCurrency(pointsSummary.cashbackAvailableCents),
            ),
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onPrimary,
        )
      }
    }
  }
}

@Composable
private fun QuickActionsSection(
  pendingPaymentAmountCents: Long?,
  onPayClick: () -> Unit,
  onRegisterVehicleClick: () -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.sm)) {
    Text(
      text = stringResource(R.string.home_quick_actions_title),
      style = MaterialTheme.typography.titleMedium,
      modifier = Modifier.semantics { heading() },
    )

    val payLabel =
      if (pendingPaymentAmountCents != null && pendingPaymentAmountCents > 0L) {
        stringResource(
          R.string.home_action_pay_pending,
          BrFormatters.formatCurrency(pendingPaymentAmountCents),
        )
      } else {
        stringResource(R.string.home_action_pay)
      }

    AutoBoxButton(
      text = payLabel,
      onClick = onPayClick,
      leadingIcon = Icons.Rounded.QrCode2,
      modifier = Modifier.fillMaxWidth(),
      testTag = "home_pay_shortcut",
    )

    AutoBoxButton(
      text = stringResource(R.string.home_action_register_vehicle),
      onClick = onRegisterVehicleClick,
      variant = AutoBoxButtonVariant.SECONDARY,
      leadingIcon = Icons.Rounded.AddCircleOutline,
      modifier = Modifier.fillMaxWidth(),
      testTag = "home_register_vehicle_shortcut",
    )
  }
}

@Composable
private fun NextReminderSection(reminder: ServiceReminder) {
  Column(verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.sm)) {
    Text(
      text = stringResource(R.string.home_next_reminder_title),
      style = MaterialTheme.typography.titleMedium,
      modifier = Modifier.semantics { heading() },
    )

    AutoBoxCard(
      variant = AutoBoxCardVariant.OUTLINED,
      modifier = Modifier.fillMaxWidth(),
      testTag = "home_next_reminder_card",
    ) {
      val (tone, statusLabel, statusIcon) =
        when (reminder.priority) {
          ReminderPriority.DUE_SOON ->
            Triple(
              AutoBoxStatusTone.WARNING,
              stringResource(R.string.home_reminder_status_due_soon),
              Icons.Rounded.WarningAmber,
            )
          ReminderPriority.UPCOMING ->
            Triple(
              AutoBoxStatusTone.INFO,
              stringResource(R.string.home_reminder_status_upcoming),
              Icons.Rounded.Schedule,
            )
          ReminderPriority.INFO ->
            Triple(
              AutoBoxStatusTone.NEUTRAL,
              stringResource(R.string.home_reminder_status_info),
              Icons.Rounded.Info,
            )
        }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        AutoBoxStatusChip(
          label = statusLabel,
          tone = tone,
          icon = statusIcon,
        )
        Text(
          text = reminder.vehicleLabel,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Spacer(modifier = Modifier.height(AutoBoxSpacing.sm))

      Row(verticalAlignment = Alignment.Top) {
        Icon(
          imageVector = Icons.Rounded.Build,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(AutoBoxSpacing.xs))
        Column(verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xxs)) {
          Text(
            text = reminder.title,
            style = MaterialTheme.typography.titleMedium,
          )
          Text(
            text = reminder.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Spacer(modifier = Modifier.height(AutoBoxSpacing.sm))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Rounded.CalendarMonth,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(AutoBoxSpacing.xxs))
        Text(
          text =
            stringResource(
              R.string.home_reminder_due_date,
              BrFormatters.formatDate(reminder.dueDate),
            ),
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@AutoBoxComponentPreviews
@Composable
private fun HomeScreenSuccessPreview() {
  AutoBoxTheme {
    HomeScreen(
      uiState =
        HomeUiState.Success(
          summary =
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
                  model = "Hyundai HB20 Comfort",
                  year = 2023,
                ),
              nextReminder =
                ServiceReminder(
                  id = "r1",
                  title = "Troca de óleo e filtro",
                  description = "Recomendado a cada 10.000 km ou 6 meses.",
                  vehicleLabel = "HB20 · BRA2E19",
                  dueDate = LocalDate.of(2026, 12, 7),
                  priority = ReminderPriority.DUE_SOON,
                ),
              pendingPaymentAmountCents = 18990L,
            ),
        ),
      isOffline = false,
      onRefresh = {},
      onRetry = {},
      onPayClick = {},
      onRegisterVehicleClick = {},
      onOpenRewardsClick = {},
    )
  }
}

@AutoBoxComponentPreviews
@Composable
private fun HomeScreenLoadingPreview() {
  AutoBoxTheme {
    HomeScreen(
      uiState = HomeUiState.Loading,
      isOffline = true,
      onRefresh = {},
      onRetry = {},
      onPayClick = {},
      onRegisterVehicleClick = {},
      onOpenRewardsClick = {},
    )
  }
}

@AutoBoxComponentPreviews
@Composable
private fun HomeScreenEmptyPreview() {
  AutoBoxTheme {
    HomeScreen(
      uiState = HomeUiState.Empty,
      isOffline = false,
      onRefresh = {},
      onRetry = {},
      onPayClick = {},
      onRegisterVehicleClick = {},
      onOpenRewardsClick = {},
    )
  }
}
