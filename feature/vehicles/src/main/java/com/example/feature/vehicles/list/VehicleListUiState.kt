package com.example.feature.vehicles.list

import androidx.compose.runtime.Immutable
import com.example.core.common.text.UiText
import com.example.core.data.model.Vehicle
import com.example.core.designsystem.components.AutoBoxStatusTone

@Immutable
sealed interface VehicleListUiState {
  data object Loading : VehicleListUiState

  data object Empty : VehicleListUiState

  data class Error(val message: UiText) : VehicleListUiState

  data class Success(
    val vehicles: List<Vehicle>,
    val vehiclePendingDeletion: Vehicle? = null,
  ) : VehicleListUiState
}

sealed interface VehicleListEvent {
  data class ShowSnackbar(
    val message: UiText,
    val tone: AutoBoxStatusTone = AutoBoxStatusTone.INFO,
  ) : VehicleListEvent

  data class ShowUndoDeleteSnackbar(
    val deletedVehicle: Vehicle,
  ) : VehicleListEvent
}
