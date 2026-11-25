package com.example.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.preview.AutoBoxComponentPreviews
import com.example.core.designsystem.theme.AutoBoxElevation
import com.example.core.designsystem.theme.AutoBoxSpacing
import com.example.core.designsystem.theme.AutoBoxTheme

enum class AutoBoxCardVariant {
  ELEVATED,
  OUTLINED,
  TONAL,
}

@Composable
fun AutoBoxCard(
  modifier: Modifier = Modifier,
  variant: AutoBoxCardVariant = AutoBoxCardVariant.ELEVATED,
  onClick: (() -> Unit)? = null,
  contentPadding: PaddingValues = PaddingValues(AutoBoxSpacing.md),
  testTag: String? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  val shape = MaterialTheme.shapes.medium
  val colors =
    when (variant) {
      AutoBoxCardVariant.ELEVATED ->
        CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.onSurface,
        )
      AutoBoxCardVariant.OUTLINED ->
        CardDefaults.outlinedCardColors(
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.onSurface,
        )
      AutoBoxCardVariant.TONAL ->
        CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
          contentColor = MaterialTheme.colorScheme.onSurface,
        )
    }
  val elevation =
    when (variant) {
      AutoBoxCardVariant.ELEVATED ->
        CardDefaults.cardElevation(defaultElevation = AutoBoxElevation.low)
      AutoBoxCardVariant.OUTLINED,
      AutoBoxCardVariant.TONAL ->
        CardDefaults.cardElevation(defaultElevation = AutoBoxElevation.none)
    }
  val border =
    when (variant) {
      AutoBoxCardVariant.OUTLINED ->
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
      AutoBoxCardVariant.ELEVATED,
      AutoBoxCardVariant.TONAL -> null
    }

  val cardModifier =
    modifier
      .then(if (onClick != null) Modifier.minimumInteractiveComponentSize() else Modifier)
      .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)

  if (onClick != null) {
    Card(
      onClick = onClick,
      modifier = cardModifier,
      shape = shape,
      colors = colors,
      elevation = elevation,
      border = border,
    ) {
      Column(modifier = Modifier.padding(contentPadding), content = content)
    }
  } else {
    Card(
      modifier = cardModifier,
      shape = shape,
      colors = colors,
      elevation = elevation,
      border = border,
    ) {
      Column(modifier = Modifier.padding(contentPadding), content = content)
    }
  }
}

@AutoBoxComponentPreviews
@Composable
private fun AutoBoxCardPreview() {
  AutoBoxTheme {
    Column(modifier = Modifier.padding(AutoBoxSpacing.md)) {
      AutoBoxCard(
        modifier = Modifier.fillMaxWidth(),
        variant = AutoBoxCardVariant.OUTLINED,
        onClick = {},
      ) {
        Text(text = "Hyundai HB20 Comfort", style = MaterialTheme.typography.titleMedium)
        Text(
          text = "Placa BRA2E19 · Revisão em dia",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}
