package com.example.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.R
import com.example.core.designsystem.preview.AutoBoxComponentPreviews
import com.example.core.designsystem.theme.AutoBoxSpacing
import com.example.core.designsystem.theme.AutoBoxTheme
import com.example.core.designsystem.theme.AutoBoxTouchTarget

enum class AutoBoxButtonVariant {
  PRIMARY,
  SECONDARY,
  TEXT,
}

@Composable
fun AutoBoxButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  variant: AutoBoxButtonVariant = AutoBoxButtonVariant.PRIMARY,
  enabled: Boolean = true,
  isLoading: Boolean = false,
  leadingIcon: ImageVector? = null,
  testTag: String? = null,
) {
  val loadingDescription = stringResource(R.string.status_loading_button)
  val isClickable = enabled && !isLoading
  val contentPadding =
    PaddingValues(horizontal = AutoBoxSpacing.lg, vertical = AutoBoxSpacing.sm)

  val baseModifier =
    modifier
      .defaultMinSize(minWidth = AutoBoxTouchTarget.min, minHeight = AutoBoxTouchTarget.min)
      .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
      .semantics {
        if (isLoading) {
          stateDescription = loadingDescription
        }
      }

  val buttonContent: @Composable () -> Unit = {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(18.dp),
          strokeWidth = 2.dp,
          color =
            when (variant) {
              AutoBoxButtonVariant.PRIMARY -> MaterialTheme.colorScheme.onPrimary
              AutoBoxButtonVariant.SECONDARY,
              AutoBoxButtonVariant.TEXT -> MaterialTheme.colorScheme.primary
            },
        )
        Spacer(modifier = Modifier.width(AutoBoxSpacing.xs))
      } else if (leadingIcon != null) {
        Icon(
          imageVector = leadingIcon,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(AutoBoxSpacing.xs))
      }
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }

  when (variant) {
    AutoBoxButtonVariant.PRIMARY ->
      Button(
        onClick = onClick,
        enabled = isClickable,
        shape = MaterialTheme.shapes.medium,
        contentPadding = contentPadding,
        colors =
          ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
          ),
        modifier = baseModifier,
      ) {
        buttonContent()
      }

    AutoBoxButtonVariant.SECONDARY ->
      OutlinedButton(
        onClick = onClick,
        enabled = isClickable,
        shape = MaterialTheme.shapes.medium,
        contentPadding = contentPadding,
        border =
          BorderStroke(
            width = 1.dp,
            color =
              if (isClickable) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.outlineVariant,
          ),
        modifier = baseModifier,
      ) {
        buttonContent()
      }

    AutoBoxButtonVariant.TEXT ->
      TextButton(
        onClick = onClick,
        enabled = isClickable,
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = AutoBoxSpacing.md, vertical = AutoBoxSpacing.xs),
        modifier = baseModifier,
      ) {
        buttonContent()
      }
  }
}

@AutoBoxComponentPreviews
@Composable
private fun AutoBoxButtonPreview() {
  AutoBoxTheme {
    Column(
      modifier = Modifier.padding(AutoBoxSpacing.md),
      verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.sm),
    ) {
      AutoBoxButton(
        text = "Pagar com Pix",
        onClick = {},
        leadingIcon = Icons.Rounded.QrCode2,
        modifier = Modifier.fillMaxWidth(),
      )
      AutoBoxButton(
        text = "Processando pagamento",
        onClick = {},
        isLoading = true,
        modifier = Modifier.fillMaxWidth(),
      )
      AutoBoxButton(
        text = "Cadastrar veículo",
        onClick = {},
        variant = AutoBoxButtonVariant.SECONDARY,
        modifier = Modifier.fillMaxWidth(),
      )
      AutoBoxButton(
        text = "Pular por enquanto",
        onClick = {},
        variant = AutoBoxButtonVariant.TEXT,
      )
    }
  }
}
