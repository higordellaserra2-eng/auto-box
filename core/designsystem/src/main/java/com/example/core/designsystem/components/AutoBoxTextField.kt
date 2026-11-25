package com.example.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.R
import com.example.core.designsystem.preview.AutoBoxComponentPreviews
import com.example.core.designsystem.theme.AutoBoxSpacing
import com.example.core.designsystem.theme.AutoBoxTheme

@Composable
fun AutoBoxTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  modifier: Modifier = Modifier,
  placeholder: String? = null,
  helperText: String? = null,
  errorMessage: String? = null,
  leadingIcon: ImageVector? = null,
  showClearButton: Boolean = true,
  enabled: Boolean = true,
  singleLine: Boolean = true,
  maxLines: Int = if (singleLine) 1 else 4,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  testTag: String? = null,
) {
  val isError = !errorMessage.isNullOrBlank()
  val clearContentDescription = stringResource(R.string.cd_clear_input)

  Column(modifier = modifier) {
    OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text(text = label) },
      placeholder = placeholder?.let { { Text(text = it) } },
      leadingIcon =
        leadingIcon?.let { icon ->
          {
            Icon(
              imageVector = icon,
              contentDescription = null,
            )
          }
        },
      trailingIcon =
        when {
          isError -> {
            {
              Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = stringResource(R.string.cd_error_icon),
                tint = MaterialTheme.colorScheme.error,
              )
            }
          }
          showClearButton && value.isNotEmpty() && enabled -> {
            {
              IconButton(
                onClick = { onValueChange("") },
                modifier = Modifier.minimumInteractiveComponentSize(),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Clear,
                  contentDescription = clearContentDescription,
                )
              }
            }
          }
          else -> null
        },
      isError = isError,
      enabled = enabled,
      singleLine = singleLine,
      maxLines = maxLines,
      visualTransformation = visualTransformation,
      keyboardOptions = keyboardOptions,
      keyboardActions = keyboardActions,
      shape = MaterialTheme.shapes.small,
      colors =
        OutlinedTextFieldDefaults.colors(
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline,
          errorBorderColor = MaterialTheme.colorScheme.error,
        ),
      supportingText = {
        when {
          isError -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error,
              )
              Spacer(modifier = Modifier.width(AutoBoxSpacing.xxs))
              Text(
                text = errorMessage.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
              )
            }
          }
          !helperText.isNullOrBlank() -> {
            Text(
              text = helperText,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              style = MaterialTheme.typography.bodySmall,
            )
          }
        }
      },
      modifier =
        Modifier
          .fillMaxWidth()
          .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
          .semantics {
            if (isError) {
              error(errorMessage.orEmpty())
            }
          },
    )
  }
}

@AutoBoxComponentPreviews
@Composable
private fun AutoBoxTextFieldPreview() {
  AutoBoxTheme {
    Column(
      modifier = Modifier.padding(AutoBoxSpacing.md),
      verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.sm),
    ) {
      AutoBoxTextField(
        value = "BRA2E19",
        onValueChange = {},
        label = "Placa do veículo",
        helperText = "Aceita padrão Mercosul ou antigo",
        leadingIcon = Icons.Rounded.DirectionsCar,
      )
      AutoBoxTextField(
        value = "AB12",
        onValueChange = {},
        label = "Placa do veículo",
        errorMessage = "Informe uma placa válida (ex.: BRA2E19).",
        leadingIcon = Icons.Rounded.DirectionsCar,
      )
    }
  }
}
