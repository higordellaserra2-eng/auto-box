@file:OptIn(ExperimentalFoundationApi::class)

package com.example.feature.vehicles.form

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Commute
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.common.format.BrFormatters
import com.example.core.common.format.PlatePattern
import com.example.core.data.model.VehicleCatalog
import com.example.core.data.model.VehicleType
import com.example.core.designsystem.components.AutoBoxButton
import com.example.core.designsystem.components.AutoBoxCard
import com.example.core.designsystem.components.AutoBoxCardVariant
import com.example.core.designsystem.components.AutoBoxFilterChip
import com.example.core.designsystem.components.AutoBoxStatusChip
import com.example.core.designsystem.components.AutoBoxStatusTone
import com.example.core.designsystem.components.AutoBoxTextField
import com.example.core.designsystem.components.LoadingState
import com.example.core.designsystem.preview.AutoBoxComponentPreviews
import com.example.core.designsystem.theme.AutoBoxMonoTypography
import com.example.core.designsystem.theme.AutoBoxSpacing
import com.example.core.designsystem.theme.AutoBoxTheme
import com.example.core.designsystem.theme.AutoBoxTouchTarget
import com.example.feature.vehicles.R

private enum class CatalogSheetTarget {
  BRAND,
  MODEL,
  YEAR,
}

@Composable
fun VehicleFormRoute(
  vehicleId: String?,
  onSaved: (String) -> Unit,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: VehicleFormViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current

  LaunchedEffect(vehicleId) {
    viewModel.initForVehicle(vehicleId)
  }

  val plateFocusRequester = remember { FocusRequester() }
  val brandFocusRequester = remember { FocusRequester() }
  val modelFocusRequester = remember { FocusRequester() }
  val yearFocusRequester = remember { FocusRequester() }
  val mileageFocusRequester = remember { FocusRequester() }

  val plateBringIntoView = remember { BringIntoViewRequester() }
  val brandBringIntoView = remember { BringIntoViewRequester() }
  val modelBringIntoView = remember { BringIntoViewRequester() }
  val yearBringIntoView = remember { BringIntoViewRequester() }
  val mileageBringIntoView = remember { BringIntoViewRequester() }

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        is VehicleFormEvent.Saved -> onSaved(event.message.asString(context))
        VehicleFormEvent.NavigateBack -> onNavigateBack()
        is VehicleFormEvent.FocusField -> {
          when (event.target) {
            VehicleFormFocusTarget.PLATE -> {
              plateBringIntoView.bringIntoView()
              plateFocusRequester.requestFocus()
            }
            VehicleFormFocusTarget.BRAND -> {
              brandBringIntoView.bringIntoView()
              brandFocusRequester.requestFocus()
            }
            VehicleFormFocusTarget.MODEL -> {
              modelBringIntoView.bringIntoView()
              modelFocusRequester.requestFocus()
            }
            VehicleFormFocusTarget.YEAR -> {
              yearBringIntoView.bringIntoView()
              yearFocusRequester.requestFocus()
            }
            VehicleFormFocusTarget.MILEAGE -> {
              mileageBringIntoView.bringIntoView()
              mileageFocusRequester.requestFocus()
            }
          }
        }
      }
    }
  }

  BackHandler(enabled = uiState.hasUnsavedChanges && !uiState.isSaving) {
    viewModel.onBackAttempt()
  }

  VehicleFormScreen(
    uiState = uiState,
    onPlateChanged = viewModel::onPlateChanged,
    onRetryLookup = viewModel::retryPlateLookup,
    onTypeSelected = viewModel::onTypeSelected,
    onBrandSelected = viewModel::onBrandSelected,
    onModelSelected = viewModel::onModelSelected,
    onYearSelected = viewModel::onYearSelected,
    onNicknameChanged = viewModel::onNicknameChanged,
    onColorChanged = viewModel::onColorChanged,
    onMileageChanged = viewModel::onMileageChanged,
    onPrimaryChanged = viewModel::onPrimaryChanged,
    onToggleMoreDetails = viewModel::onToggleMoreDetails,
    onSaveClick = viewModel::saveClick,
    onDismissDiscardDialog = viewModel::dismissDiscardDialog,
    onConfirmDiscard = viewModel::confirmDiscard,
    plateFocusRequester = plateFocusRequester,
    brandFocusRequester = brandFocusRequester,
    modelFocusRequester = modelFocusRequester,
    yearFocusRequester = yearFocusRequester,
    mileageFocusRequester = mileageFocusRequester,
    plateBringIntoView = plateBringIntoView,
    brandBringIntoView = brandBringIntoView,
    modelBringIntoView = modelBringIntoView,
    yearBringIntoView = yearBringIntoView,
    mileageBringIntoView = mileageBringIntoView,
    modifier = modifier,
  )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun VehicleFormScreen(
  uiState: VehicleFormUiState,
  onPlateChanged: (String) -> Unit,
  onRetryLookup: () -> Unit,
  onTypeSelected: (VehicleType) -> Unit,
  onBrandSelected: (String) -> Unit,
  onModelSelected: (String) -> Unit,
  onYearSelected: (Int) -> Unit,
  onNicknameChanged: (String) -> Unit,
  onColorChanged: (String) -> Unit,
  onMileageChanged: (String) -> Unit,
  onPrimaryChanged: (Boolean) -> Unit,
  onToggleMoreDetails: () -> Unit,
  onSaveClick: () -> Unit,
  onDismissDiscardDialog: () -> Unit,
  onConfirmDiscard: () -> Unit,
  modifier: Modifier = Modifier,
  plateFocusRequester: FocusRequester = remember { FocusRequester() },
  brandFocusRequester: FocusRequester = remember { FocusRequester() },
  modelFocusRequester: FocusRequester = remember { FocusRequester() },
  yearFocusRequester: FocusRequester = remember { FocusRequester() },
  mileageFocusRequester: FocusRequester = remember { FocusRequester() },
  plateBringIntoView: BringIntoViewRequester = remember { BringIntoViewRequester() },
  brandBringIntoView: BringIntoViewRequester = remember { BringIntoViewRequester() },
  modelBringIntoView: BringIntoViewRequester = remember { BringIntoViewRequester() },
  yearBringIntoView: BringIntoViewRequester = remember { BringIntoViewRequester() },
  mileageBringIntoView: BringIntoViewRequester = remember { BringIntoViewRequester() },
) {
  if (uiState.isInitialLoading) {
    LoadingState(skeletonCount = 3, modifier = modifier.fillMaxSize())
    return
  }

  var activeSheet by remember { mutableStateOf<CatalogSheetTarget?>(null) }

  if (uiState.showDiscardDialog) {
    AlertDialog(
      onDismissRequest = onDismissDiscardDialog,
      title = { Text(text = stringResource(R.string.vehicle_form_discard_title)) },
      text = { Text(text = stringResource(R.string.vehicle_form_discard_message)) },
      confirmButton = {
        TextButton(
          onClick = onConfirmDiscard,
          modifier = Modifier.testTag("vehicle_form_discard_confirm"),
        ) {
          Text(
            text = stringResource(R.string.vehicle_form_discard_confirm),
            color = MaterialTheme.colorScheme.error,
          )
        }
      },
      dismissButton = {
        TextButton(
          onClick = onDismissDiscardDialog,
          modifier = Modifier.testTag("vehicle_form_discard_cancel"),
        ) {
          Text(text = stringResource(R.string.vehicle_form_discard_cancel))
        }
      },
    )
  }

  activeSheet?.let { sheetTarget ->
    when (sheetTarget) {
      CatalogSheetTarget.BRAND ->
        VehicleCatalogPickerSheet(
          title = stringResource(R.string.vehicle_catalog_select_brand),
          options = VehicleCatalog.brandsFor(uiState.type),
          selectedValue = uiState.brand,
          allowCustomValue = true,
          onSelect = onBrandSelected,
          onDismiss = { activeSheet = null },
        )
      CatalogSheetTarget.MODEL ->
        VehicleCatalogPickerSheet(
          title = stringResource(R.string.vehicle_catalog_select_model),
          options = VehicleCatalog.modelsFor(uiState.type, uiState.brand),
          selectedValue = uiState.model,
          allowCustomValue = true,
          onSelect = onModelSelected,
          onDismiss = { activeSheet = null },
        )
      CatalogSheetTarget.YEAR ->
        VehicleCatalogPickerSheet(
          title = stringResource(R.string.vehicle_catalog_select_year),
          options = VehicleCatalog.years().map { it.toString() },
          selectedValue = uiState.year?.toString().orEmpty(),
          allowCustomValue = false,
          numericSearch = true,
          onSelect = { selected ->
            selected.toIntOrNull()?.let(onYearSelected)
          },
          onDismiss = { activeSheet = null },
        )
    }
  }

  val scrollState = rememberScrollState()

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier =
        Modifier
          .widthIn(max = 600.dp)
          .fillMaxSize()
          .verticalScroll(scrollState)
          .imePadding()
          .padding(horizontal = AutoBoxSpacing.md, vertical = AutoBoxSpacing.md)
          .testTag("vehicle_form_container"),
      verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.md),
    ) {
      // 1. Placa primeiro
      Column(
        modifier = Modifier.bringIntoViewRequester(plateBringIntoView),
        verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs),
      ) {
        AutoBoxTextField(
          value = uiState.plate,
          onValueChange = onPlateChanged,
          label = stringResource(R.string.vehicle_form_plate_label),
          placeholder = stringResource(R.string.vehicle_form_plate_placeholder),
          helperText =
            if (uiState.isEditMode) {
              stringResource(R.string.vehicle_form_plate_readonly_helper)
            } else {
              stringResource(R.string.vehicle_form_plate_helper)
            },
          errorMessage = uiState.plateError?.asString(),
          leadingIcon = Icons.Rounded.Pin,
          enabled = !uiState.isEditMode && !uiState.isSaving,
          showClearButton = !uiState.isEditMode,
          textStyle = AutoBoxMonoTypography.plate,
          visualTransformation = remember { PlateVisualTransformation() },
          keyboardOptions =
            KeyboardOptions(
              capitalization = KeyboardCapitalization.Characters,
              keyboardType = KeyboardType.Ascii,
              autoCorrectEnabled = false,
              imeAction = ImeAction.Next,
            ),
          keyboardActions =
            KeyboardActions(
              onNext = { brandFocusRequester.requestFocus() },
            ),
          inputModifier = Modifier.focusRequester(plateFocusRequester),
          testTag = "vehicle_plate_input",
        )

        if (!uiState.isEditMode && uiState.platePattern != PlatePattern.INVALID) {
          val patternLabel =
            when (uiState.platePattern) {
              PlatePattern.MERCOSUL -> stringResource(R.string.vehicle_form_plate_pattern_mercosul)
              PlatePattern.LEGACY -> stringResource(R.string.vehicle_form_plate_pattern_legacy)
              PlatePattern.INVALID -> ""
            }
          AutoBoxStatusChip(
            label = patternLabel,
            tone = AutoBoxStatusTone.SUCCESS,
            icon = Icons.Rounded.CheckCircle,
            modifier = Modifier.testTag("vehicle_plate_pattern_badge"),
          )
        }

        if (!uiState.isEditMode) {
          PlateLookupFeedbackBanner(
            status = uiState.lookupStatus,
            onRetryLookup = onRetryLookup,
          )
        }
      }

      // 2. Tipo de veículo
      Column(verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs)) {
        Text(
          text = stringResource(R.string.vehicle_form_type_label),
          style = MaterialTheme.typography.titleSmall,
          modifier = Modifier.semantics { heading() },
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs),
          verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs),
        ) {
          AutoBoxFilterChip(
            label = stringResource(R.string.vehicles_type_car),
            selected = uiState.type == VehicleType.CAR,
            onClick = { onTypeSelected(VehicleType.CAR) },
            leadingIcon = Icons.Rounded.DirectionsCar,
            testTag = "vehicle_type_car",
          )
          AutoBoxFilterChip(
            label = stringResource(R.string.vehicles_type_motorcycle),
            selected = uiState.type == VehicleType.MOTORCYCLE,
            onClick = { onTypeSelected(VehicleType.MOTORCYCLE) },
            leadingIcon = Icons.Rounded.TwoWheeler,
            testTag = "vehicle_type_motorcycle",
          )
          AutoBoxFilterChip(
            label = stringResource(R.string.vehicles_type_other),
            selected = uiState.type == VehicleType.OTHER,
            onClick = { onTypeSelected(VehicleType.OTHER) },
            leadingIcon = Icons.Rounded.Commute,
            testTag = "vehicle_type_other",
          )
        }
      }

      // 3. Marca, Modelo e Ano com seletores pesquisáveis
      VehicleCatalogSelectorField(
        value = uiState.brand,
        label = stringResource(R.string.vehicle_form_brand_label),
        placeholder = stringResource(R.string.vehicle_form_brand_placeholder),
        errorMessage = uiState.brandError?.asString(),
        leadingIcon = Icons.Rounded.DirectionsCar,
        enabled = !uiState.isSaving,
        focusRequester = brandFocusRequester,
        bringIntoViewRequester = brandBringIntoView,
        testTag = "vehicle_brand_field",
        onClick = { activeSheet = CatalogSheetTarget.BRAND },
      )

      VehicleCatalogSelectorField(
        value = uiState.model,
        label = stringResource(R.string.vehicle_form_model_label),
        placeholder = stringResource(R.string.vehicle_form_model_placeholder),
        errorMessage = uiState.modelError?.asString(),
        leadingIcon = Icons.Rounded.DirectionsCar,
        enabled = !uiState.isSaving,
        focusRequester = modelFocusRequester,
        bringIntoViewRequester = modelBringIntoView,
        testTag = "vehicle_model_field",
        onClick = { activeSheet = CatalogSheetTarget.MODEL },
      )

      VehicleCatalogSelectorField(
        value = uiState.year?.toString().orEmpty(),
        label = stringResource(R.string.vehicle_form_year_label),
        placeholder = stringResource(R.string.vehicle_form_year_placeholder),
        errorMessage = uiState.yearError?.asString(),
        leadingIcon = Icons.Rounded.CalendarMonth,
        enabled = !uiState.isSaving,
        focusRequester = yearFocusRequester,
        bringIntoViewRequester = yearBringIntoView,
        testTag = "vehicle_year_field",
        onClick = { activeSheet = CatalogSheetTarget.YEAR },
      )

      // 4. Seção recolhível "Mais detalhes"
      val expandedDescription =
        if (uiState.isMoreDetailsExpanded) {
          stringResource(R.string.vehicle_form_more_details_expanded_cd)
        } else {
          stringResource(R.string.vehicle_form_more_details_collapsed_cd)
        }
      Surface(
        onClick = onToggleMoreDetails,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier =
          Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AutoBoxTouchTarget.min)
            .testTag("vehicle_more_details_toggle")
            .semantics { stateDescription = expandedDescription },
      ) {
        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(horizontal = AutoBoxSpacing.md, vertical = AutoBoxSpacing.sm),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = stringResource(R.string.vehicle_form_more_details_title),
            style = MaterialTheme.typography.titleSmall,
          )
          Icon(
            imageVector =
              if (uiState.isMoreDetailsExpanded) Icons.Rounded.ExpandLess
              else Icons.Rounded.ExpandMore,
            contentDescription = null,
          )
        }
      }

      AnimatedVisibility(visible = uiState.isMoreDetailsExpanded) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.sm),
        ) {
          AutoBoxTextField(
            value = uiState.nickname,
            onValueChange = onNicknameChanged,
            label = stringResource(R.string.vehicle_form_nickname_label),
            placeholder = stringResource(R.string.vehicle_form_nickname_placeholder),
            leadingIcon = Icons.Rounded.Tag,
            enabled = !uiState.isSaving,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            testTag = "vehicle_nickname_input",
          )

          AutoBoxTextField(
            value = uiState.color,
            onValueChange = onColorChanged,
            label = stringResource(R.string.vehicle_form_color_label),
            placeholder = stringResource(R.string.vehicle_form_color_placeholder),
            leadingIcon = Icons.Rounded.Palette,
            enabled = !uiState.isSaving,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            testTag = "vehicle_color_input",
          )

          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs),
          ) {
            VehicleCatalog.commonColors.forEach { colorName ->
              AutoBoxFilterChip(
                label = colorName,
                selected = uiState.color.equals(colorName, ignoreCase = true),
                onClick = { onColorChanged(colorName) },
              )
            }
          }

          Box(modifier = Modifier.bringIntoViewRequester(mileageBringIntoView)) {
            AutoBoxTextField(
              value = uiState.mileageText,
              onValueChange = onMileageChanged,
              label = stringResource(R.string.vehicle_form_mileage_label),
              placeholder = stringResource(R.string.vehicle_form_mileage_placeholder),
              errorMessage = uiState.mileageError?.asString(),
              leadingIcon = Icons.Rounded.Speed,
              enabled = !uiState.isSaving,
              keyboardOptions =
                KeyboardOptions(
                  keyboardType = KeyboardType.Number,
                  imeAction = ImeAction.Done,
                ),
              inputModifier = Modifier.focusRequester(mileageFocusRequester),
              testTag = "vehicle_mileage_input",
            )
          }
        }
      }

      // 5. Definir como veículo principal
      Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .defaultMinSize(minHeight = AutoBoxTouchTarget.min)
              .toggleable(
                value = uiState.isPrimary,
                enabled = uiState.canTogglePrimary && !uiState.isSaving,
                role = Role.Switch,
                onValueChange = onPrimaryChanged,
              )
              .padding(horizontal = AutoBoxSpacing.md, vertical = AutoBoxSpacing.sm)
              .testTag("vehicle_primary_switch"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(AutoBoxSpacing.sm),
        ) {
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            Text(
              text = stringResource(R.string.vehicle_form_primary_switch_title),
              style = MaterialTheme.typography.titleSmall,
            )
            Text(
              text =
                if (uiState.canTogglePrimary) {
                  stringResource(R.string.vehicle_form_primary_switch_subtitle)
                } else {
                  stringResource(R.string.vehicle_form_primary_locked_subtitle)
                },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Switch(
            checked = uiState.isPrimary,
            onCheckedChange = null,
            enabled = uiState.canTogglePrimary && !uiState.isSaving,
          )
        }
      }

      // 6. Resumo de confirmação antes de salvar
      if (uiState.canShowSummary) {
        AutoBoxCard(
          variant = AutoBoxCardVariant.TONAL,
          modifier = Modifier.fillMaxWidth(),
          testTag = "vehicle_form_summary_card",
        ) {
          Text(
            text = stringResource(R.string.vehicle_form_summary_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics { heading() },
          )
          Spacer(modifier = Modifier.height(AutoBoxSpacing.xs))
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs),
          ) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.primaryContainer,
              contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
              Text(
                text = BrFormatters.formatPlate(uiState.plate),
                style = AutoBoxMonoTypography.plate,
                modifier = Modifier.padding(horizontal = AutoBoxSpacing.xs, vertical = 2.dp),
              )
            }
            if (uiState.isPrimary) {
              AutoBoxStatusChip(
                label = stringResource(R.string.vehicles_primary_badge),
                tone = AutoBoxStatusTone.INFO,
                icon = Icons.Rounded.Star,
              )
            }
          }
          Spacer(modifier = Modifier.height(AutoBoxSpacing.xs))
          Text(
            text = "${uiState.brand} ${uiState.model} · ${uiState.year}",
            style = MaterialTheme.typography.titleMedium,
          )
          if (uiState.nickname.isNotBlank()) {
            Text(
              text = uiState.nickname,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      uiState.saveError?.let { errorMsg ->
        AutoBoxStatusChip(
          label = errorMsg.asString(),
          tone = AutoBoxStatusTone.ERROR,
          modifier =
            Modifier
              .fillMaxWidth()
              .semantics { liveRegion = LiveRegionMode.Polite },
        )
      }

      AutoBoxButton(
        text =
          if (uiState.isEditMode) {
            stringResource(R.string.vehicle_form_update_button)
          } else {
            stringResource(R.string.vehicle_form_save_button)
          },
        onClick = onSaveClick,
        isLoading = uiState.isSaving,
        enabled = !uiState.isSaving,
        modifier = Modifier.fillMaxWidth(),
        testTag = "vehicle_form_save_button",
      )

      Spacer(modifier = Modifier.height(AutoBoxSpacing.md))
    }
  }
}

@Composable
private fun PlateLookupFeedbackBanner(
  status: PlateLookupStatus,
  onRetryLookup: () -> Unit,
) {
  when (status) {
    PlateLookupStatus.Idle -> Unit
    PlateLookupStatus.Loading -> {
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .testTag("vehicle_lookup_loading")
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs),
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(16.dp),
          strokeWidth = 2.dp,
        )
        Text(
          text = stringResource(R.string.vehicle_form_lookup_loading),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    PlateLookupStatus.Found -> {
      AutoBoxStatusChip(
        label = stringResource(R.string.vehicle_form_lookup_found),
        tone = AutoBoxStatusTone.INFO,
        icon = Icons.Rounded.CheckCircle,
        modifier =
          Modifier
            .testTag("vehicle_lookup_found")
            .semantics { liveRegion = LiveRegionMode.Polite },
      )
    }
    PlateLookupStatus.NotFound -> {
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .testTag("vehicle_lookup_not_found")
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs),
      ) {
        Icon(
          imageVector = Icons.Rounded.Info,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(16.dp),
        )
        Text(
          text = stringResource(R.string.vehicle_form_lookup_not_found),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    is PlateLookupStatus.Error -> {
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .testTag("vehicle_lookup_error")
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = stringResource(R.string.vehicle_form_lookup_error),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.weight(1f),
        )
        TextButton(
          onClick = onRetryLookup,
          modifier = Modifier.defaultMinSize(minHeight = AutoBoxTouchTarget.min),
        ) {
          Text(text = stringResource(R.string.vehicle_form_lookup_retry))
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VehicleCatalogSelectorField(
  value: String,
  label: String,
  placeholder: String,
  errorMessage: String?,
  leadingIcon: ImageVector,
  enabled: Boolean,
  focusRequester: FocusRequester,
  bringIntoViewRequester: BringIntoViewRequester,
  testTag: String,
  onClick: () -> Unit,
) {
  val openLabel = stringResource(R.string.vehicle_catalog_open_cd, label)
  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .bringIntoViewRequester(bringIntoViewRequester),
  ) {
    AutoBoxTextField(
      value = value,
      onValueChange = {},
      label = label,
      placeholder = placeholder,
      errorMessage = errorMessage,
      leadingIcon = leadingIcon,
      trailingIcon = {
        Icon(
          imageVector = Icons.Rounded.KeyboardArrowDown,
          contentDescription = null,
        )
      },
      enabled = enabled,
      readOnly = true,
      showClearButton = false,
    )
    Box(
      modifier =
        Modifier
          .matchParentSize()
          .focusRequester(focusRequester)
          .focusable(enabled = enabled)
          .testTag(testTag)
          .semantics {
            contentDescription =
              if (value.isBlank()) "$label. $openLabel" else "$label: $value. $openLabel"
          }
          .clickable(
            enabled = enabled,
            onClickLabel = openLabel,
            onClick = onClick,
          ),
    )
  }
}

@AutoBoxComponentPreviews
@Composable
private fun VehicleFormCreatePreview() {
  AutoBoxTheme {
    Surface {
      VehicleFormScreen(
        uiState =
          VehicleFormUiState(
            plate = "BRA2E19",
            platePattern = PlatePattern.MERCOSUL,
            lookupStatus = PlateLookupStatus.Found,
            type = VehicleType.CAR,
            brand = "Hyundai",
            model = "HB20",
            year = 2023,
            color = "Prata",
            isPrimary = true,
            canTogglePrimary = false,
          ),
        onPlateChanged = {},
        onRetryLookup = {},
        onTypeSelected = {},
        onBrandSelected = {},
        onModelSelected = {},
        onYearSelected = {},
        onNicknameChanged = {},
        onColorChanged = {},
        onMileageChanged = {},
        onPrimaryChanged = {},
        onToggleMoreDetails = {},
        onSaveClick = {},
        onDismissDiscardDialog = {},
        onConfirmDiscard = {},
      )
    }
  }
}

@AutoBoxComponentPreviews
@Composable
private fun VehicleFormErrorPreview() {
  AutoBoxTheme {
    Surface {
      VehicleFormScreen(
        uiState =
          VehicleFormUiState(
            plate = "1234567",
            platePattern = PlatePattern.INVALID,
            plateError = com.example.core.common.text.UiText.DynamicString("Placa inválida. Use o formato AAA-9999 ou AAA9A99."),
            lookupStatus = PlateLookupStatus.NotFound,
          ),
        onPlateChanged = {},
        onRetryLookup = {},
        onTypeSelected = {},
        onBrandSelected = {},
        onModelSelected = {},
        onYearSelected = {},
        onNicknameChanged = {},
        onColorChanged = {},
        onMileageChanged = {},
        onPrimaryChanged = {},
        onToggleMoreDetails = {},
        onSaveClick = {},
        onDismissDiscardDialog = {},
        onConfirmDiscard = {},
      )
    }
  }
}
