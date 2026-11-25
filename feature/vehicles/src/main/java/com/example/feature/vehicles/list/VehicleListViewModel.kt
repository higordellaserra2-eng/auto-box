package com.example.feature.vehicles.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.connectivity.ConnectivityMonitor
import com.example.core.common.format.BrFormatters
import com.example.core.common.result.AppResult
import com.example.core.common.text.UiText
import com.example.core.data.model.Vehicle
import com.example.core.data.repository.VehicleRepository
import com.example.core.designsystem.components.AutoBoxStatusTone
import com.example.feature.vehicles.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class VehicleListViewModel @Inject constructor(
  private val vehicleRepository: VehicleRepository,
  connectivityMonitor: ConnectivityMonitor,
) : ViewModel() {

  private val pendingDeletion = MutableStateFlow<Vehicle?>(null)
  private val retryTrigger = MutableStateFlow(0)

  private val _events = Channel<VehicleListEvent>(Channel.BUFFERED)
  val events = _events.receiveAsFlow()

  val isOffline: StateFlow<Boolean> =
    connectivityMonitor.isOnline
      .map { online -> !online }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
      )

  val uiState: StateFlow<VehicleListUiState> =
    combine(
      vehicleRepository.observeVehicles()
        .map<List<Vehicle>, VehicleListUiState> { list ->
          if (list.isEmpty()) VehicleListUiState.Empty else VehicleListUiState.Success(list)
        }
        .catch { throwable ->
          emit(
            VehicleListUiState.Error(
              UiText.DynamicString(
                throwable.localizedMessage ?: "",
              ).takeIf { !throwable.localizedMessage.isNullOrBlank() }
                ?: UiText.StringResource(R.string.vehicles_error_title),
            ),
          )
        },
      pendingDeletion,
      retryTrigger,
    ) { baseState, deleteCandidate, _ ->
      when (baseState) {
        is VehicleListUiState.Success ->
          baseState.copy(vehiclePendingDeletion = deleteCandidate)
        else -> baseState
      }
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = VehicleListUiState.Loading,
    )

  fun requestDelete(vehicle: Vehicle) {
    pendingDeletion.value = vehicle
  }

  fun dismissDeleteDialog() {
    pendingDeletion.value = null
  }

  fun confirmDelete() {
    val target = pendingDeletion.value ?: return
    pendingDeletion.value = null
    viewModelScope.launch {
      when (val result = vehicleRepository.deleteVehicle(target.id)) {
        is AppResult.Success -> {
          val deleted = result.data ?: target
          _events.send(VehicleListEvent.ShowUndoDeleteSnackbar(deleted))
        }
        is AppResult.Error -> {
          _events.send(
            VehicleListEvent.ShowSnackbar(
              message = result.error.userMessage,
              tone = AutoBoxStatusTone.ERROR,
            ),
          )
        }
        AppResult.Loading -> Unit
      }
    }
  }

  fun undoDelete(vehicle: Vehicle) {
    viewModelScope.launch {
      when (val result = vehicleRepository.restoreVehicle(vehicle)) {
        is AppResult.Success -> {
          _events.send(
            VehicleListEvent.ShowSnackbar(
              message =
                UiText.StringResource(
                  R.string.vehicles_restored_snackbar,
                  BrFormatters.formatPlate(vehicle.plate),
                ),
              tone = AutoBoxStatusTone.SUCCESS,
            ),
          )
        }
        is AppResult.Error -> {
          _events.send(
            VehicleListEvent.ShowSnackbar(
              message = result.error.userMessage,
              tone = AutoBoxStatusTone.ERROR,
            ),
          )
        }
        AppResult.Loading -> Unit
      }
    }
  }

  fun setPrimaryVehicle(vehicle: Vehicle) {
    if (vehicle.isPrimary) return
    viewModelScope.launch {
      when (val result = vehicleRepository.setPrimaryVehicle(vehicle.id)) {
        is AppResult.Success -> {
          _events.send(
            VehicleListEvent.ShowSnackbar(
              message =
                UiText.StringResource(
                  R.string.vehicles_primary_updated_snackbar,
                  vehicle.brand,
                  vehicle.model,
                ),
              tone = AutoBoxStatusTone.SUCCESS,
            ),
          )
        }
        is AppResult.Error -> {
          _events.send(
            VehicleListEvent.ShowSnackbar(
              message = result.error.userMessage,
              tone = AutoBoxStatusTone.ERROR,
            ),
          )
        }
        AppResult.Loading -> Unit
      }
    }
  }

  fun retry() {
    retryTrigger.value += 1
  }
}
