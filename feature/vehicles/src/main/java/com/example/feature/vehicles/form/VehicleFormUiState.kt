package com.example.feature.vehicles.form

import androidx.compose.runtime.Immutable
import com.example.core.common.format.PlatePattern
import com.example.core.common.text.UiText
import com.example.core.data.model.VehicleType

@Immutable
sealed interface PlateLookupStatus {
  data object Idle : PlateLookupStatus

  data object Loading : PlateLookupStatus

  data object Found : PlateLookupStatus

  data object NotFound : PlateLookupStatus

  data class Error(val message: UiText) : PlateLookupStatus
}

enum class VehicleFormFocusTarget {
  PLATE,
  BRAND,
  MODEL,
  YEAR,
  MILEAGE,
}

@Immutable
data class VehicleFormUiState(
  val vehicleId: String? = null,
  val isEditMode: Boolean = false,
  val isInitialLoading: Boolean = false,
  val plate: String = "",
  val platePattern: PlatePattern = PlatePattern.INVALID,
  val plateError: UiText? = null,
  val lookupStatus: PlateLookupStatus = PlateLookupStatus.Idle,
  val type: VehicleType = VehicleType.CAR,
  val brand: String = "",
  val brandError: UiText? = null,
  val model: String = "",
  val modelError: UiText? = null,
  val year: Int? = null,
  val yearError: UiText? = null,
  val color: String = "",
  val nickname: String = "",
  val mileageText: String = "",
  val mileageError: UiText? = null,
  val isPrimary: Boolean = true,
  val canTogglePrimary: Boolean = true,
  val isMoreDetailsExpanded: Boolean = false,
  val isSaving: Boolean = false,
  val saveError: UiText? = null,
  val hasUnsavedChanges: Boolean = false,
  val showDiscardDialog: Boolean = false,
) {
  val canShowSummary: Boolean
    get() = plate.length == 7 && brand.isNotBlank() && model.isNotBlank() && year != null
}

sealed interface VehicleFormEvent {
  data class Saved(val message: UiText) : VehicleFormEvent

  data class FocusField(val target: VehicleFormFocusTarget) : VehicleFormEvent

  data object NavigateBack : VehicleFormEvent
}
