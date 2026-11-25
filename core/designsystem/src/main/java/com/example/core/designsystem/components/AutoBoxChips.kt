package com.example.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.preview.AutoBoxComponentPreviews
import com.example.core.designsystem.theme.AutoBoxSpacing
import com.example.core.designsystem.theme.AutoBoxTheme
import com.example.core.designsystem.theme.AutoBoxTouchTarget

enum class AutoBoxStatusTone {
  SUCCESS,
  WARNING,
  ERROR,
  INFO,
  NEUTRAL,
}

@Composable
fun AutoBoxFilterChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  leadingIcon: ImageVector? = null,
  testTag: String? = null,
) {
  FilterChip(
    selected = selected,
    onClick = onClick,
    label = {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    },
    leadingIcon =
      when {
        selected -> {
          {
            Icon(
              imageVector = Icons.Rounded.Check,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
          }
        }
        leadingIcon != null -> {
          {
            Icon(
              imageVector = leadingIcon,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
          }
        }
        else -> null
      },
    modifier =
      modifier
        .defaultMinSize(minHeight = AutoBoxTouchTarget.min)
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
  )
}

@Composable
fun AutoBoxStatusChip(
  label: String,
  tone: AutoBoxStatusTone,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
) {
  val semanticColors = AutoBoxTheme.semanticColors
  val (containerColor, contentColor, defaultIcon) =
    when (tone) {
      AutoBoxStatusTone.SUCCESS ->
        Triple(
          semanticColors.successContainer,
          semanticColors.onSuccessContainer,
          Icons.Rounded.CheckCircle,
        )
      AutoBoxStatusTone.WARNING ->
        Triple(
          semanticColors.warningContainer,
          semanticColors.onWarningContainer,
          Icons.Rounded.Schedule,
        )
      AutoBoxStatusTone.ERROR ->
        Triple(
          MaterialTheme.colorScheme.errorContainer,
          MaterialTheme.colorScheme.onErrorContainer,
          Icons.Rounded.ErrorOutline,
        )
      AutoBoxStatusTone.INFO ->
        Triple(
          semanticColors.infoContainer,
          semanticColors.onInfoContainer,
          Icons.Rounded.Info,
        )
      AutoBoxStatusTone.NEUTRAL ->
        Triple(
          MaterialTheme.colorScheme.surfaceVariant,
          MaterialTheme.colorScheme.onSurfaceVariant,
          Icons.Rounded.Info,
        )
    }

  Surface(
    modifier = modifier.semantics(mergeDescendants = true) {},
    shape = MaterialTheme.shapes.small,
    color = containerColor,
    contentColor = contentColor,
  ) {
    Row(
      modifier =
        Modifier.padding(
          horizontal = AutoBoxSpacing.sm,
          vertical = AutoBoxSpacing.xxs + 2.dp,
        ),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = icon ?: defaultIcon,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
      )
      Spacer(modifier = Modifier.width(AutoBoxSpacing.xxs + 2.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@AutoBoxComponentPreviews
@Composable
private fun AutoBoxChipsPreview() {
  AutoBoxTheme {
    FlowRow(
      modifier = Modifier.padding(AutoBoxSpacing.md),
      horizontalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs),
      verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs),
    ) {
      AutoBoxFilterChip(label = "Todos", selected = true, onClick = {})
      AutoBoxFilterChip(label = "Lavagem", selected = false, onClick = {})
      AutoBoxStatusChip(label = "Pago via Pix", tone = AutoBoxStatusTone.SUCCESS)
      AutoBoxStatusChip(label = "Vence em 12 dias", tone = AutoBoxStatusTone.WARNING)
      AutoBoxStatusChip(label = "Falha no cartão", tone = AutoBoxStatusTone.ERROR)
    }
  }
}
