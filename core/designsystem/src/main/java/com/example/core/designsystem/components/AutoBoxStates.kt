package com.example.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.R
import com.example.core.designsystem.preview.AutoBoxComponentPreviews
import com.example.core.designsystem.theme.AutoBoxSpacing
import com.example.core.designsystem.theme.AutoBoxTheme

@Composable
fun LoadingState(
  modifier: Modifier = Modifier,
  skeletonCount: Int = 3,
  contentDescription: String = stringResource(R.string.cd_loading),
) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(AutoBoxSpacing.md)
        .testTag("loading_state")
        .semantics {
          this.contentDescription = contentDescription
          liveRegion = LiveRegionMode.Polite
        },
    verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.sm),
  ) {
    repeat(skeletonCount) {
      AutoBoxCardSkeleton()
    }
  }
}

@Composable
fun EmptyState(
  title: String,
  description: String,
  modifier: Modifier = Modifier,
  icon: ImageVector = Icons.Rounded.DirectionsCar,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(AutoBoxSpacing.lg)
        .testTag("empty_state"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Surface(
      shape = CircleShape,
      color = MaterialTheme.colorScheme.primaryContainer,
      contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
      modifier = Modifier.size(64.dp),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier = Modifier.size(32.dp),
        )
      }
    }
    Spacer(modifier = Modifier.height(AutoBoxSpacing.md))
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge,
      textAlign = TextAlign.Center,
      modifier = Modifier.semantics { heading() },
    )
    Spacer(modifier = Modifier.height(AutoBoxSpacing.xs))
    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
    )
    if (!actionLabel.isNullOrBlank() && onAction != null) {
      Spacer(modifier = Modifier.height(AutoBoxSpacing.lg))
      AutoBoxButton(
        text = actionLabel,
        onClick = onAction,
        testTag = "empty_state_cta",
      )
    }
  }
}

@Composable
fun ErrorState(
  title: String,
  message: String,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
  retryLabel: String = stringResource(R.string.action_retry),
) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(AutoBoxSpacing.lg)
        .testTag("error_state")
        .semantics { liveRegion = LiveRegionMode.Polite },
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Surface(
      shape = CircleShape,
      color = MaterialTheme.colorScheme.errorContainer,
      contentColor = MaterialTheme.colorScheme.onErrorContainer,
      modifier = Modifier.size(64.dp),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = Icons.Rounded.ErrorOutline,
          contentDescription = stringResource(R.string.cd_error_icon),
          modifier = Modifier.size(32.dp),
        )
      }
    }
    Spacer(modifier = Modifier.height(AutoBoxSpacing.md))
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge,
      textAlign = TextAlign.Center,
      modifier = Modifier.semantics { heading() },
    )
    Spacer(modifier = Modifier.height(AutoBoxSpacing.xs))
    Text(
      text = message,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(AutoBoxSpacing.lg))
    AutoBoxButton(
      text = retryLabel,
      onClick = onRetry,
      leadingIcon = Icons.Rounded.Refresh,
      variant = AutoBoxButtonVariant.SECONDARY,
      testTag = "error_state_retry_button",
    )
  }
}

@Composable
fun OfflineBanner(
  isOffline: Boolean,
  modifier: Modifier = Modifier,
  message: String = stringResource(R.string.offline_banner_message),
) {
  AnimatedVisibility(
    visible = isOffline,
    enter = expandVertically(),
    exit = shrinkVertically(),
    modifier = modifier,
  ) {
    val semanticColors = AutoBoxTheme.semanticColors
    Surface(
      color = semanticColors.warningContainer,
      contentColor = semanticColors.onWarningContainer,
      modifier =
        Modifier
          .fillMaxWidth()
          .testTag("offline_banner")
          .semantics(mergeDescendants = true) {
            liveRegion = LiveRegionMode.Polite
          },
    ) {
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(horizontal = AutoBoxSpacing.md, vertical = AutoBoxSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.Rounded.CloudOff,
          contentDescription = stringResource(R.string.cd_offline_icon),
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(AutoBoxSpacing.xs))
        Text(
          text = message,
          style = MaterialTheme.typography.bodySmall,
        )
      }
    }
  }
}

@AutoBoxComponentPreviews
@Composable
private fun AutoBoxStatesPreview() {
  AutoBoxTheme {
    Column(verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.md)) {
      OfflineBanner(isOffline = true)
      EmptyState(
        title = "Nenhum veículo cadastrado",
        description = "Adicione a placa do seu carro para receber lembretes e pagar mais rápido.",
        actionLabel = "Cadastrar veículo",
        onAction = {},
      )
      ErrorState(
        title = "Não foi possível carregar",
        message = "Confira sua conexão e tente novamente.",
        onRetry = {},
      )
    }
  }
}
