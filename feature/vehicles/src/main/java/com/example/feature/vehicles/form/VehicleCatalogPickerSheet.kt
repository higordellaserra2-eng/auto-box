package com.example.feature.vehicles.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.components.AutoBoxTextField
import com.example.core.designsystem.preview.AutoBoxComponentPreviews
import com.example.core.designsystem.theme.AutoBoxSpacing
import com.example.core.designsystem.theme.AutoBoxTheme
import com.example.core.designsystem.theme.AutoBoxTouchTarget
import com.example.feature.vehicles.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleCatalogPickerSheet(
  title: String,
  options: List<String>,
  selectedValue: String,
  allowCustomValue: Boolean,
  numericSearch: Boolean = false,
  onSelect: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
  ) {
    VehicleCatalogPickerContent(
      title = title,
      options = options,
      selectedValue = selectedValue,
      allowCustomValue = allowCustomValue,
      numericSearch = numericSearch,
      onSelect = { value ->
        onSelect(value)
        onDismiss()
      },
    )
  }
}

@Composable
internal fun VehicleCatalogPickerContent(
  title: String,
  options: List<String>,
  selectedValue: String,
  allowCustomValue: Boolean,
  numericSearch: Boolean = false,
  onSelect: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var query by remember { mutableStateOf("") }
  val trimmedQuery = query.trim()

  val filteredOptions =
    remember(options, trimmedQuery) {
      if (trimmedQuery.isEmpty()) {
        options
      } else {
        options.filter { it.contains(trimmedQuery, ignoreCase = true) }
      }
    }

  val exactMatchExists =
    remember(options, trimmedQuery) {
      options.any { it.equals(trimmedQuery, ignoreCase = true) }
    }

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = AutoBoxSpacing.md)
        .padding(bottom = AutoBoxSpacing.lg),
    verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.sm),
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge,
      modifier = Modifier.semantics { heading() },
    )

    AutoBoxTextField(
      value = query,
      onValueChange = { query = it },
      label = stringResource(R.string.vehicle_catalog_search_label),
      placeholder = stringResource(R.string.vehicle_catalog_search_placeholder),
      leadingIcon = Icons.Rounded.Search,
      keyboardOptions =
        KeyboardOptions(
          keyboardType = if (numericSearch) KeyboardType.Number else KeyboardType.Text,
          imeAction = ImeAction.Search,
        ),
      testTag = "catalog_picker_search_input",
    )

    LazyColumn(
      modifier =
        Modifier
          .fillMaxWidth()
          .heightIn(min = 180.dp, max = 360.dp)
          .testTag("catalog_picker_list"),
      contentPadding = PaddingValues(vertical = AutoBoxSpacing.xxs),
    ) {
      if (allowCustomValue && trimmedQuery.isNotEmpty() && !exactMatchExists) {
        item(key = "custom_value") {
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            shape = MaterialTheme.shapes.small,
            modifier =
              Modifier
                .fillMaxWidth()
                .padding(bottom = AutoBoxSpacing.xs),
          ) {
            Row(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .defaultMinSize(minHeight = AutoBoxTouchTarget.min)
                  .clickable { onSelect(trimmedQuery) }
                  .padding(horizontal = AutoBoxSpacing.md, vertical = AutoBoxSpacing.sm)
                  .testTag("catalog_picker_custom_option"),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs),
            ) {
              Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
              )
              Text(
                text = stringResource(R.string.vehicle_catalog_use_custom, trimmedQuery),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            }
          }
        }
      }

      if (filteredOptions.isEmpty() && (!allowCustomValue || trimmedQuery.isEmpty())) {
        item(key = "empty") {
          Text(
            text = stringResource(R.string.vehicle_catalog_empty, trimmedQuery),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = AutoBoxSpacing.lg),
          )
        }
      } else {
        items(
          items = filteredOptions,
          key = { it },
        ) { option ->
          val isSelected = option.equals(selectedValue, ignoreCase = true)
          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = AutoBoxTouchTarget.min)
                .clickable { onSelect(option) }
                .padding(horizontal = AutoBoxSpacing.xs, vertical = AutoBoxSpacing.sm)
                .testTag("catalog_option_$option"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = option,
              style =
                if (isSelected) {
                  MaterialTheme.typography.titleSmall
                } else {
                  MaterialTheme.typography.bodyLarge
                },
              color =
                if (isSelected) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.onSurface
                },
            )
            if (isSelected) {
              Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = stringResource(R.string.vehicle_picker_selected_cd),
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          }
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
      }
    }

    Spacer(modifier = Modifier.height(AutoBoxSpacing.xs))
  }
}

@AutoBoxComponentPreviews
@Composable
private fun VehicleCatalogPickerPreview() {
  AutoBoxTheme {
    Surface {
      VehicleCatalogPickerContent(
        title = "Selecionar marca",
        options = listOf("BYD", "Chevrolet", "Fiat", "Honda", "Hyundai", "Toyota", "Volkswagen"),
        selectedValue = "Hyundai",
        allowCustomValue = true,
        onSelect = {},
      )
    }
  }
}
