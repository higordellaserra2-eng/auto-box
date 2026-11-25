package com.example.feature.vehicles.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Commute
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.common.format.BrFormatters
import com.example.core.data.model.Vehicle
import com.example.core.data.model.VehicleType
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
import com.example.core.designsystem.theme.AutoBoxTouchTarget
import com.example.feature.vehicles.R
import com.example.feature.vehicles.form.VehicleFormRoute
import kotlinx.coroutines.launch

private sealed interface VehiclesDetailPane {
  data object Placeholder : VehiclesDetailPane

  data object Create : VehiclesDetailPane

  data class Edit(val vehicleId: String) : VehiclesDetailPane
}

@Composable
fun VehicleListRoute(
  widthSizeClass: WindowWidthSizeClass,
  onNavigateToCreate: () -> Unit,
  onNavigateToEdit: (String) -> Unit,
  onShowSnackbar: suspend (message: String, actionLabel: String?, tone: AutoBoxStatusTone, onAction: (() -> Unit)?) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: VehicleListViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        is VehicleListEvent.ShowSnackbar -> {
          onShowSnackbar(event.message.asString(context), null, event.tone, null)
        }
        is VehicleListEvent.ShowUndoDeleteSnackbar -> {
          val deleted = event.deletedVehicle
          val message =
            context.getString(
              R.string.vehicles_deleted_snackbar,
              BrFormatters.formatPlate(deleted.plate),
            )
          val undoLabel = context.getString(R.string.vehicles_undo_action)
          onShowSnackbar(message, undoLabel, AutoBoxStatusTone.INFO) {
            viewModel.undoDelete(deleted)
          }
        }
      }
    }
  }

  val isExpanded = widthSizeClass == WindowWidthSizeClass.Expanded
  var detailMode by rememberSaveable { mutableStateOf("placeholder") }
  var editingVehicleId by rememberSaveable { mutableStateOf<String?>(null) }

  val activeDetailPane: VehiclesDetailPane =
    when (detailMode) {
      "create" -> VehiclesDetailPane.Create
      "edit" -> editingVehicleId?.let { VehiclesDetailPane.Edit(it) } ?: VehiclesDetailPane.Placeholder
      else -> VehiclesDetailPane.Placeholder
    }

  if (isExpanded) {
    Row(modifier = modifier.fillMaxSize()) {
      VehicleListScreen(
        uiState = uiState,
        isOffline = isOffline,
        selectedVehicleId = (activeDetailPane as? VehiclesDetailPane.Edit)?.vehicleId,
        onAddVehicleClick = {
          editingVehicleId = null
          detailMode = "create"
        },
        onEditVehicleClick = { vehicle ->
          editingVehicleId = vehicle.id
          detailMode = "edit"
        },
        onSetPrimaryClick = viewModel::setPrimaryVehicle,
        onRequestDeleteClick = viewModel::requestDelete,
        onConfirmDelete = {
          val deletingId = (uiState as? VehicleListUiState.Success)?.vehiclePendingDeletion?.id
          if (deletingId != null && deletingId == editingVehicleId) {
            editingVehicleId = null
            detailMode = "placeholder"
          }
          viewModel.confirmDelete()
        },
        onDismissDeleteDialog = viewModel::dismissDeleteDialog,
        onRetry = viewModel::retry,
        modifier = Modifier.weight(0.46f).fillMaxHeight(),
      )

      VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

      Box(
        modifier = Modifier.weight(0.54f).fillMaxHeight(),
      ) {
        when (val pane = activeDetailPane) {
          VehiclesDetailPane.Placeholder -> {
            EmptyState(
              title = stringResource(R.string.vehicles_detail_placeholder_title),
              description = stringResource(R.string.vehicles_detail_placeholder_desc),
              icon = Icons.Rounded.DirectionsCar,
              actionLabel = stringResource(R.string.vehicles_add_fab),
              onAction = {
                editingVehicleId = null
                detailMode = "create"
              },
              modifier = Modifier.align(Alignment.Center),
            )
          }
          VehiclesDetailPane.Create -> {
            key("inline_create") {
              VehicleFormRoute(
                vehicleId = null,
                onSaved = { message ->
                  detailMode = "placeholder"
                  scope.launch {
                    onShowSnackbar(message, null, AutoBoxStatusTone.SUCCESS, null)
                  }
                },
                onNavigateBack = { detailMode = "placeholder" },
              )
            }
          }
          is VehiclesDetailPane.Edit -> {
            key("inline_edit_${pane.vehicleId}") {
              VehicleFormRoute(
                vehicleId = pane.vehicleId,
                onSaved = { message ->
                  editingVehicleId = null
                  detailMode = "placeholder"
                  scope.launch {
                    onShowSnackbar(message, null, AutoBoxStatusTone.SUCCESS, null)
                  }
                },
                onNavigateBack = {
                  editingVehicleId = null
                  detailMode = "placeholder"
                },
              )
            }
          }
        }
      }
    }
  } else {
    VehicleListScreen(
      uiState = uiState,
      isOffline = isOffline,
      selectedVehicleId = null,
      onAddVehicleClick = onNavigateToCreate,
      onEditVehicleClick = { vehicle -> onNavigateToEdit(vehicle.id) },
      onSetPrimaryClick = viewModel::setPrimaryVehicle,
      onRequestDeleteClick = viewModel::requestDelete,
      onConfirmDelete = viewModel::confirmDelete,
      onDismissDeleteDialog = viewModel::dismissDeleteDialog,
      onRetry = viewModel::retry,
      modifier = modifier,
    )
  }
}

@Composable
fun VehicleListScreen(
  uiState: VehicleListUiState,
  isOffline: Boolean,
  onAddVehicleClick: () -> Unit,
  onEditVehicleClick: (Vehicle) -> Unit,
  onSetPrimaryClick: (Vehicle) -> Unit,
  onRequestDeleteClick: (Vehicle) -> Unit,
  onConfirmDelete: () -> Unit,
  onDismissDeleteDialog: () -> Unit,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
  selectedVehicleId: String? = null,
) {
  val pendingDelete = (uiState as? VehicleListUiState.Success)?.vehiclePendingDeletion

  if (pendingDelete != null) {
    AlertDialog(
      onDismissRequest = onDismissDeleteDialog,
      title = { Text(text = stringResource(R.string.vehicles_delete_dialog_title)) },
      text = {
        Text(
          text =
            stringResource(
              R.string.vehicles_delete_dialog_message,
              pendingDelete.brand,
              pendingDelete.model,
              BrFormatters.formatPlate(pendingDelete.plate),
            ),
        )
      },
      confirmButton = {
        TextButton(
          onClick = onConfirmDelete,
          modifier = Modifier.testTag("vehicles_delete_confirm_button"),
        ) {
          Text(
            text = stringResource(R.string.vehicles_delete_dialog_confirm),
            color = MaterialTheme.colorScheme.error,
          )
        }
      },
      dismissButton = {
        TextButton(
          onClick = onDismissDeleteDialog,
          modifier = Modifier.testTag("vehicles_delete_cancel_button"),
        ) {
          Text(text = stringResource(R.string.vehicles_delete_dialog_cancel))
        }
      },
    )
  }

  Box(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      OfflineBanner(isOffline = isOffline)

      when (uiState) {
        VehicleListUiState.Loading -> {
          LoadingState(
            skeletonCount = 3,
            modifier = Modifier.fillMaxSize(),
          )
        }

        VehicleListUiState.Empty -> {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
          ) {
            EmptyState(
              title = stringResource(R.string.vehicles_empty_title),
              description = stringResource(R.string.vehicles_empty_description),
              icon = Icons.Rounded.DirectionsCar,
              actionLabel = stringResource(R.string.vehicles_empty_cta),
              onAction = onAddVehicleClick,
            )
          }
        }

        is VehicleListUiState.Error -> {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
          ) {
            ErrorState(
              title = stringResource(R.string.vehicles_error_title),
              message = uiState.message.asString(),
              onRetry = onRetry,
            )
          }
        }

        is VehicleListUiState.Success -> {
          LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("vehicles_list"),
            contentPadding =
              PaddingValues(
                start = AutoBoxSpacing.md,
                end = AutoBoxSpacing.md,
                top = AutoBoxSpacing.md,
                bottom = 96.dp,
              ),
            verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.sm),
          ) {
            item(key = "vehicles_count_header") {
              Text(
                text =
                  pluralStringResource(
                    id = R.plurals.vehicles_count_label,
                    count = uiState.vehicles.size,
                    uiState.vehicles.size,
                  ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { heading() },
              )
            }

            items(
              items = uiState.vehicles,
              key = { it.id },
            ) { vehicle ->
              VehicleCard(
                vehicle = vehicle,
                isSelected = vehicle.id == selectedVehicleId,
                onEditClick = { onEditVehicleClick(vehicle) },
                onSetPrimaryClick = { onSetPrimaryClick(vehicle) },
                onDeleteClick = { onRequestDeleteClick(vehicle) },
              )
            }
          }
        }
      }
    }

    if (uiState is VehicleListUiState.Success) {
      ExtendedFloatingActionButton(
        onClick = onAddVehicleClick,
        icon = {
          Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = null,
          )
        },
        text = { Text(text = stringResource(R.string.vehicles_add_fab)) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier =
          Modifier
            .align(Alignment.BottomEnd)
            .padding(AutoBoxSpacing.md)
            .defaultMinSize(minHeight = AutoBoxTouchTarget.min)
            .testTag("vehicles_fab_add"),
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VehicleCard(
  vehicle: Vehicle,
  onEditClick: () -> Unit,
  onSetPrimaryClick: () -> Unit,
  onDeleteClick: () -> Unit,
  modifier: Modifier = Modifier,
  isSelected: Boolean = false,
) {
  var menuExpanded by remember { mutableStateOf(false) }

  val formattedPlate = BrFormatters.formatPlate(vehicle.plate)
  val spokenPlate = BrFormatters.formatPlateForAccessibility(vehicle.plate)
  val baseCd =
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
  val nickname = vehicle.nickname?.takeIf { it.isNotBlank() }
  val fullCardCd =
    if (nickname != null) {
      "$baseCd ${stringResource(R.string.vehicles_card_cd_nickname, nickname)}"
    } else {
      baseCd
    }

  val editLabel = stringResource(R.string.vehicles_action_edit)
  val setPrimaryLabel = stringResource(R.string.vehicles_action_set_primary)
  val deleteLabel = stringResource(R.string.vehicles_action_delete)
  val moreOptionsCd =
    stringResource(R.string.vehicles_more_options_cd, vehicle.brand, vehicle.model, spokenPlate)

  val cardVariant =
    when {
      isSelected -> AutoBoxCardVariant.TONAL
      vehicle.isPrimary -> AutoBoxCardVariant.ELEVATED
      else -> AutoBoxCardVariant.OUTLINED
    }

  Surface(
    shape = MaterialTheme.shapes.medium,
    border =
      if (isSelected || vehicle.isPrimary) {
        BorderStroke(
          width = if (isSelected) 2.dp else 1.dp,
          color =
            if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
        )
      } else {
        null
      },
    modifier = modifier.fillMaxWidth(),
  ) {
    AutoBoxCard(
      onClick = onEditClick,
      variant = cardVariant,
      testTag = "vehicle_card_${vehicle.id}",
      modifier =
        Modifier
          .fillMaxWidth()
          .semantics(mergeDescendants = true) {
            contentDescription = fullCardCd
            customActions =
              buildList {
                add(CustomAccessibilityAction(editLabel) { onEditClick(); true })
                if (!vehicle.isPrimary) {
                  add(CustomAccessibilityAction(setPrimaryLabel) { onSetPrimaryClick(); true })
                }
                add(CustomAccessibilityAction(deleteLabel) { onDeleteClick(); true })
              }
          },
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(AutoBoxSpacing.sm),
      ) {
        Surface(
          shape = MaterialTheme.shapes.small,
          color = MaterialTheme.colorScheme.surfaceContainerLow,
          modifier = Modifier.size(44.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector =
                when (vehicle.type) {
                  VehicleType.CAR -> Icons.Rounded.DirectionsCar
                  VehicleType.MOTORCYCLE -> Icons.Rounded.TwoWheeler
                  VehicleType.OTHER -> Icons.Rounded.Commute
                },
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
          }
        }

        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs),
        ) {
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xxs),
          ) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.primaryContainer,
              contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
              Text(
                text = formattedPlate,
                style = AutoBoxMonoTypography.plate,
                modifier = Modifier.padding(horizontal = AutoBoxSpacing.xs, vertical = 3.dp),
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
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )

          val details =
            buildList {
              vehicle.nickname?.takeIf { it.isNotBlank() }?.let { add(it) }
              vehicle.color?.takeIf { it.isNotBlank() }?.let { add(it) }
              vehicle.mileageKm?.let { km ->
                add(stringResource(R.string.vehicles_mileage_label, BrFormatters.formatPoints(km)))
              }
            }
          if (details.isNotEmpty()) {
            Text(
              text = details.joinToString(" · "),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        Box {
          IconButton(
            onClick = { menuExpanded = true },
            modifier =
              Modifier
                .minimumInteractiveComponentSize()
                .testTag("vehicle_menu_${vehicle.id}"),
          ) {
            Icon(
              imageVector = Icons.Rounded.MoreVert,
              contentDescription = moreOptionsCd,
            )
          }

          DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
          ) {
            DropdownMenuItem(
              text = { Text(text = editLabel) },
              leadingIcon = { Icon(imageVector = Icons.Rounded.Edit, contentDescription = null) },
              onClick = {
                menuExpanded = false
                onEditClick()
              },
              modifier = Modifier.testTag("vehicle_menu_edit_${vehicle.id}"),
            )
            if (!vehicle.isPrimary) {
              DropdownMenuItem(
                text = { Text(text = setPrimaryLabel) },
                leadingIcon = {
                  Icon(imageVector = Icons.Rounded.StarOutline, contentDescription = null)
                },
                onClick = {
                  menuExpanded = false
                  onSetPrimaryClick()
                },
                modifier = Modifier.testTag("vehicle_menu_primary_${vehicle.id}"),
              )
            }
            DropdownMenuItem(
              text = {
                Text(
                  text = deleteLabel,
                  color = MaterialTheme.colorScheme.error,
                )
              },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Rounded.DeleteOutline,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.error,
                )
              },
              onClick = {
                menuExpanded = false
                onDeleteClick()
              },
              modifier = Modifier.testTag("vehicle_menu_delete_${vehicle.id}"),
            )
          }
        }
      }
    }
  }
}

@AutoBoxComponentPreviews
@Composable
private fun VehicleListSuccessPreview() {
  val vehicles =
    listOf(
      Vehicle(
        id = "1",
        plate = "BRA2E19",
        type = VehicleType.CAR,
        brand = "Hyundai",
        model = "HB20",
        year = 2023,
        color = "Prata",
        nickname = "Meu HB20",
        mileageKm = 24500,
        isPrimary = true,
      ),
      Vehicle(
        id = "2",
        plate = "ABC1234",
        type = VehicleType.MOTORCYCLE,
        brand = "Honda",
        model = "CG 160 Titan",
        year = 2024,
        color = "Preto",
        isPrimary = false,
      ),
    )
  AutoBoxTheme {
    Surface {
      VehicleListScreen(
        uiState = VehicleListUiState.Success(vehicles),
        isOffline = false,
        onAddVehicleClick = {},
        onEditVehicleClick = {},
        onSetPrimaryClick = {},
        onRequestDeleteClick = {},
        onConfirmDelete = {},
        onDismissDeleteDialog = {},
        onRetry = {},
      )
    }
  }
}

@AutoBoxComponentPreviews
@Composable
private fun VehicleListEmptyPreview() {
  AutoBoxTheme {
    Surface {
      VehicleListScreen(
        uiState = VehicleListUiState.Empty,
        isOffline = true,
        onAddVehicleClick = {},
        onEditVehicleClick = {},
        onSetPrimaryClick = {},
        onRequestDeleteClick = {},
        onConfirmDelete = {},
        onDismissDeleteDialog = {},
        onRetry = {},
      )
    }
  }
}
