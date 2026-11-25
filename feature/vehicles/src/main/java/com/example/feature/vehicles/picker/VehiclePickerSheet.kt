package com.example.feature.vehicles.picker

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.core.common.format.BrFormatters
import com.example.core.data.model.Vehicle
import com.example.core.data.model.VehicleType
import com.example.core.designsystem.components.AutoBoxButton
import com.example.core.designsystem.components.AutoBoxButtonVariant
import com.example.core.designsystem.components.AutoBoxStatusChip
import com.example.core.designsystem.components.AutoBoxStatusTone
import com.example.core.designsystem.components.EmptyState
import com.example.core.designsystem.preview.AutoBoxComponentPreviews
import com.example.core.designsystem.theme.AutoBoxMonoTypography
import com.example.core.designsystem.theme.AutoBoxSpacing
import com.example.core.designsystem.theme.AutoBoxTheme
import com.example.core.designsystem.theme.AutoBoxTouchTarget
import com.example.feature.vehicles.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclePickerSheet(
  vehicles: List<Vehicle>,
  selectedVehicleId: String?,
  onSelectVehicle: (Vehicle) -> Unit,
  onAddNewVehicle: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    modifier = modifier,
  ) {
    VehiclePickerSheetContent(
      vehicles = vehicles,
      selectedVehicleId = selectedVehicleId,
      onSelectVehicle = { vehicle ->
        onSelectVehicle(vehicle)
        onDismiss()
      },
      onAddNewVehicle = {
        onDismiss()
        onAddNewVehicle()
      },
    )
  }
}

@Composable
internal fun VehiclePickerSheetContent(
  vehicles: List<Vehicle>,
  selectedVehicleId: String?,
  onSelectVehicle: (Vehicle) -> Unit,
  onAddNewVehicle: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = AutoBoxSpacing.md)
        .padding(bottom = AutoBoxSpacing.lg)
        .testTag("vehicle_picker_sheet"),
    verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.sm),
  ) {
    Text(
      text = stringResource(R.string.vehicle_picker_title),
      style = MaterialTheme.typography.titleLarge,
      modifier = Modifier.semantics { heading() },
    )
    Text(
      text = stringResource(R.string.vehicle_picker_subtitle),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    if (vehicles.isEmpty()) {
      EmptyState(
        title = stringResource(R.string.vehicle_picker_empty_title),
        description = stringResource(R.string.vehicle_picker_empty_desc),
        icon = Icons.Rounded.DirectionsCar,
        actionLabel = stringResource(R.string.vehicle_picker_add_new),
        onAction = onAddNewVehicle,
      )
    } else {
      LazyColumn(
        modifier =
          Modifier
            .fillMaxWidth()
            .heightIn(max = 340.dp)
            .testTag("vehicle_picker_list"),
        contentPadding = PaddingValues(vertical = AutoBoxSpacing.xxs),
        verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs),
      ) {
        items(
          items = vehicles,
          key = { it.id },
        ) { vehicle ->
          val isSelected = vehicle.id == selectedVehicleId
          val formattedPlate = BrFormatters.formatPlate(vehicle.plate)
          val spokenPlate = BrFormatters.formatPlateForAccessibility(vehicle.plate)
          val itemCd =
            if (vehicle.isPrimary) {
              stringResource(
                R.string.vehicles_card_cd_primary,
                vehicle.brand,
                vehicle.model,
                vehicle.year,
                spokenPlate,
              )
            } else {
              stringResource(
                R.string.vehicles_card_cd_regular,
                vehicle.brand,
                vehicle.model,
                vehicle.year,
                spokenPlate,
              )
            }

          Surface(
            onClick = { onSelectVehicle(vehicle) },
            shape = MaterialTheme.shapes.medium,
            color =
              if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
              } else {
                MaterialTheme.colorScheme.surfaceContainerLow
              },
            border =
              if (isSelected) {
                BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
              } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
              },
            modifier =
              Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = AutoBoxTouchTarget.min)
                .testTag("vehicle_picker_item_${vehicle.id}")
                .semantics(mergeDescendants = true) {
                  role = Role.RadioButton
                  selected = isSelected
                  contentDescription = itemCd
                },
          ) {
            Row(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .padding(AutoBoxSpacing.sm),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(AutoBoxSpacing.sm),
            ) {
              Icon(
                imageVector =
                  if (vehicle.type == VehicleType.MOTORCYCLE) {
                    Icons.Rounded.TwoWheeler
                  } else {
                    Icons.Rounded.DirectionsCar
                  },
                contentDescription = null,
                tint =
                  if (isSelected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.onSurfaceVariant,
              )

              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xxs),
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs),
                ) {
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                  ) {
                    Text(
                      text = formattedPlate,
                      style = AutoBoxMonoTypography.plate,
                      modifier = Modifier.padding(horizontal = AutoBoxSpacing.xs, vertical = 2.dp),
                    )
                  }
                  if (vehicle.isPrimary) {
                    AutoBoxStatusChip(
                      label = stringResource(R.string.vehicles_primary_badge),
                      tone = AutoBoxStatusTone.INFO,
                      icon = Icons.Rounded.Star,
                    )
                  }
                }

                Text(
                  text = "${vehicle.brand} ${vehicle.model} · ${vehicle.year}",
                  style = MaterialTheme.typography.titleSmall,
                )
              }

              Icon(
                imageVector =
                  if (isSelected) Icons.Rounded.CheckCircle
                  else Icons.Rounded.RadioButtonUnchecked,
                contentDescription =
                  if (isSelected) stringResource(R.string.vehicle_picker_selected_cd) else null,
                tint =
                  if (isSelected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(22.dp),
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(AutoBoxSpacing.xxs))

      AutoBoxButton(
        text = stringResource(R.string.vehicle_picker_add_new),
        onClick = onAddNewVehicle,
        variant = AutoBoxButtonVariant.SECONDARY,
        leadingIcon = Icons.Rounded.AddCircleOutline,
        modifier = Modifier.fillMaxWidth(),
        testTag = "vehicle_picker_add_new_button",
      )
    }
  }
}

@AutoBoxComponentPreviews
@Composable
private fun VehiclePickerSheetPreview() {
  val sampleVehicles =
    listOf(
      Vehicle(
        id = "1",
        plate = "BRA2E19",
        type = VehicleType.CAR,
        brand = "Hyundai",
        model = "HB20",
        year = 2023,
        isPrimary = true,
      ),
      Vehicle(
        id = "2",
        plate = "ABC1234",
        type = VehicleType.CAR,
        brand = "Chevrolet",
        model = "Onix",
        year = 2022,
        isPrimary = false,
      ),
    )

  AutoBoxTheme {
    Surface {
      VehiclePickerSheetContent(
        vehicles = sampleVehicles,
        selectedVehicleId = "1",
        onSelectVehicle = {},
        onAddNewVehicle = {},
      )
    }
  }
}

@AutoBoxComponentPreviews
@Composable
private fun VehiclePickerSheetEmptyPreview() {
  AutoBoxTheme {
    Surface {
      VehiclePickerSheetContent(
        vehicles = emptyList(),
        selectedVehicleId = null,
        onSelectVehicle = {},
        onAddNewVehicle = {},
      )
    }
  }
}
