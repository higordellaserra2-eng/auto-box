package com.example.feature.vehicles.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.format.BrFormatters
import com.example.core.common.format.PlatePattern
import com.example.core.common.result.AppError
import com.example.core.common.result.AppResult
import com.example.core.common.text.UiText
import com.example.core.data.model.PlateLookupResult
import com.example.core.data.model.Vehicle
import com.example.core.data.model.VehicleType
import com.example.core.data.repository.PlateLookupRepository
import com.example.core.data.repository.VehicleRepository
import com.example.feature.vehicles.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class VehicleFormViewModel @Inject constructor(
  private val savedStateHandle: SavedStateHandle,
  private val vehicleRepository: VehicleRepository,
  private val plateLookupRepository: PlateLookupRepository,
) : ViewModel() {

  private data class FormSnapshot(
    val plate: String,
    val type: VehicleType,
    val brand: String,
    val model: String,
    val year: Int?,
    val color: String,
    val nickname: String,
    val mileageText: String,
    val isPrimary: Boolean,
  )

  private val _uiState = MutableStateFlow(restoreDraftState())
  val uiState: StateFlow<VehicleFormUiState> = _uiState.asStateFlow()

  private val _events = Channel<VehicleFormEvent>(Channel.BUFFERED)
  val events = _events.receiveAsFlow()

  private var initialSnapshot: FormSnapshot? = null
  private var lookupJob: Job? = null
  private var lastLookedUpPlate: String? = null
  private var initializedVehicleKey: String? = null

  private var manuallyEditedType = false
  private var manuallyEditedBrand = false
  private var manuallyEditedModel = false
  private var manuallyEditedYear = false
  private var manuallyEditedColor = false

  init {
    val routeVehicleId = savedStateHandle.get<String>(ARG_VEHICLE_ID)
    initForVehicle(routeVehicleId)
  }

  fun initForVehicle(vehicleId: String?) {
    val key = vehicleId ?: NEW_VEHICLE_KEY
    if (initializedVehicleKey == key) return
    initializedVehicleKey = key

    val hasSavedDraft =
      savedStateHandle.get<Boolean>(KEY_DRAFT_INITIALIZED) == true &&
        savedStateHandle.get<String>(KEY_LOADED_VEHICLE_ID) == key

    if (hasSavedDraft) {
      val restored = restoreDraftState().copy(
        vehicleId = vehicleId,
        isEditMode = vehicleId != null,
      )
      _uiState.value = restored
      viewModelScope.launch {
        val count = vehicleRepository.observeVehicles().first().size
        val canToggle = if (vehicleId != null) count > 1 || !restored.isPrimary else count > 0
        _uiState.update { it.copy(canTogglePrimary = canToggle) }
      }
      return
    }

    if (vehicleId == null) {
      resetForNewVehicle()
    } else {
      loadExistingVehicle(vehicleId)
    }
  }

  private fun resetForNewVehicle() {
    lookupJob?.cancel()
    lastLookedUpPlate = null
    manuallyEditedType = false
    manuallyEditedBrand = false
    manuallyEditedModel = false
    manuallyEditedYear = false
    manuallyEditedColor = false

    viewModelScope.launch {
      val existingCount = vehicleRepository.observeVehicles().first().size
      val defaultPrimary = existingCount == 0
      val snapshot =
        FormSnapshot(
          plate = "",
          type = VehicleType.CAR,
          brand = "",
          model = "",
          year = null,
          color = "",
          nickname = "",
          mileageText = "",
          isPrimary = defaultPrimary,
        )
      initialSnapshot = snapshot
      val newState =
        VehicleFormUiState(
          vehicleId = null,
          isEditMode = false,
          isInitialLoading = false,
          isPrimary = defaultPrimary,
          canTogglePrimary = !defaultPrimary,
        )
      _uiState.value = newState
      persistDraft(key = NEW_VEHICLE_KEY, state = newState)
    }
  }

  private fun loadExistingVehicle(vehicleId: String) {
    lookupJob?.cancel()
    _uiState.update { it.copy(vehicleId = vehicleId, isEditMode = true, isInitialLoading = true) }

    viewModelScope.launch {
      val vehicle = vehicleRepository.getVehicleById(vehicleId)
      val existingCount = vehicleRepository.observeVehicles().first().size
      if (vehicle == null) {
        _uiState.update {
          it.copy(
            isInitialLoading = false,
            saveError = UiText.StringResource(R.string.vehicles_error_title),
          )
        }
        return@launch
      }

      val mileage = vehicle.mileageKm?.toString().orEmpty()
      val snapshot =
        FormSnapshot(
          plate = vehicle.plate,
          type = vehicle.type,
          brand = vehicle.brand,
          model = vehicle.model,
          year = vehicle.year,
          color = vehicle.color.orEmpty(),
          nickname = vehicle.nickname.orEmpty(),
          mileageText = mileage,
          isPrimary = vehicle.isPrimary,
        )
      initialSnapshot = snapshot
      val hasOptionalDetails =
        !vehicle.color.isNullOrBlank() || !vehicle.nickname.isNullOrBlank() || vehicle.mileageKm != null

      val loadedState =
        VehicleFormUiState(
          vehicleId = vehicle.id,
          isEditMode = true,
          isInitialLoading = false,
          plate = vehicle.plate,
          platePattern = BrFormatters.detectPlatePattern(vehicle.plate),
          type = vehicle.type,
          brand = vehicle.brand,
          model = vehicle.model,
          year = vehicle.year,
          color = vehicle.color.orEmpty(),
          nickname = vehicle.nickname.orEmpty(),
          mileageText = mileage,
          isPrimary = vehicle.isPrimary,
          canTogglePrimary = existingCount > 1 || !vehicle.isPrimary,
          isMoreDetailsExpanded = hasOptionalDetails,
          hasUnsavedChanges = false,
        )
      _uiState.value = loadedState
      persistDraft(key = vehicleId, state = loadedState)
    }
  }

  fun onPlateChanged(raw: String) {
    if (_uiState.value.isEditMode) return
    val normalized = BrFormatters.normalizePlate(raw)
    val pattern = BrFormatters.detectPlatePattern(normalized)
    val realTimeError =
      if (normalized.length == 7 && pattern == PlatePattern.INVALID) {
        UiText.StringResource(R.string.vehicle_form_plate_error_invalid)
      } else {
        null
      }

    updateState { current ->
      current.copy(
        plate = normalized,
        platePattern = pattern,
        plateError = realTimeError,
        saveError = null,
        lookupStatus =
          if (normalized.length < 7) PlateLookupStatus.Idle else current.lookupStatus,
      )
    }

    if (normalized.length < 7 || pattern == PlatePattern.INVALID) {
      lookupJob?.cancel()
      if (normalized.length < 7) {
        lastLookedUpPlate = null
      }
      return
    }

    if (normalized != lastLookedUpPlate) {
      triggerPlateLookup(normalized)
    }
  }

  fun retryPlateLookup() {
    val plate = _uiState.value.plate
    if (BrFormatters.isValidPlate(plate)) {
      lastLookedUpPlate = null
      triggerPlateLookup(plate)
    }
  }

  private fun triggerPlateLookup(normalizedPlate: String) {
    lookupJob?.cancel()
    lastLookedUpPlate = normalizedPlate
    lookupJob =
      viewModelScope.launch {
        _uiState.update { it.copy(lookupStatus = PlateLookupStatus.Loading) }
        when (val result = plateLookupRepository.lookupByPlate(normalizedPlate)) {
          is AppResult.Success -> {
            when (val payload = result.data) {
              is PlateLookupResult.Found -> {
                val info = payload.info
                updateState { current ->
                  current.copy(
                    lookupStatus = PlateLookupStatus.Found,
                    type = if (!manuallyEditedType) info.type else current.type,
                    brand = if (!manuallyEditedBrand) info.brand else current.brand,
                    brandError = if (!manuallyEditedBrand) null else current.brandError,
                    model = if (!manuallyEditedModel) info.model else current.model,
                    modelError = if (!manuallyEditedModel) null else current.modelError,
                    year = if (!manuallyEditedYear) info.year else current.year,
                    yearError = if (!manuallyEditedYear) null else current.yearError,
                    color = if (!manuallyEditedColor && !info.color.isNullOrBlank()) info.color.orEmpty() else current.color,
                  )
                }
              }
              PlateLookupResult.NotFound -> {
                _uiState.update { it.copy(lookupStatus = PlateLookupStatus.NotFound) }
              }
            }
          }
          is AppResult.Error -> {
            _uiState.update {
              it.copy(lookupStatus = PlateLookupStatus.Error(result.error.userMessage))
            }
          }
          AppResult.Loading -> Unit
        }
      }
  }

  fun onTypeSelected(type: VehicleType) {
    manuallyEditedType = true
    updateState { current ->
      val brandChanged = current.type != type
      current.copy(
        type = type,
        model = if (brandChanged) "" else current.model,
        saveError = null,
      )
    }
  }

  fun onBrandSelected(brand: String) {
    manuallyEditedBrand = true
    updateState { current ->
      val resetModel = !current.brand.equals(brand.trim(), ignoreCase = true)
      current.copy(
        brand = brand.trim(),
        brandError = null,
        model = if (resetModel) "" else current.model,
        saveError = null,
      )
    }
  }

  fun onModelSelected(model: String) {
    manuallyEditedModel = true
    updateState { current ->
      current.copy(
        model = model.trim(),
        modelError = null,
        saveError = null,
      )
    }
  }

  fun onYearSelected(year: Int) {
    manuallyEditedYear = true
    updateState { current ->
      current.copy(
        year = year,
        yearError = null,
        saveError = null,
      )
    }
  }

  fun onNicknameChanged(value: String) {
    updateState { it.copy(nickname = value.take(40), saveError = null) }
  }

  fun onColorChanged(value: String) {
    manuallyEditedColor = true
    updateState { it.copy(color = value.take(24), saveError = null) }
  }

  fun onMileageChanged(raw: String) {
    val digitsOnly = raw.filter(Char::isDigit).take(7)
    updateState { it.copy(mileageText = digitsOnly, mileageError = null, saveError = null) }
  }

  fun onPrimaryChanged(isPrimary: Boolean) {
    if (!_uiState.value.canTogglePrimary) return
    updateState { it.copy(isPrimary = isPrimary, saveError = null) }
  }

  fun onToggleMoreDetails() {
    updateState { it.copy(isMoreDetailsExpanded = !it.isMoreDetailsExpanded) }
  }

  fun onBackAttempt() {
    val state = _uiState.value
    if (state.isSaving) return
    if (state.hasUnsavedChanges) {
      _uiState.update { it.copy(showDiscardDialog = true) }
    } else {
      viewModelScope.launch { _events.send(VehicleFormEvent.NavigateBack) }
    }
  }

  fun dismissDiscardDialog() {
    _uiState.update { it.copy(showDiscardDialog = false) }
  }

  fun confirmDiscard() {
    _uiState.update { it.copy(showDiscardDialog = false) }
    viewModelScope.launch { _events.send(VehicleFormEvent.NavigateBack) }
  }

  fun saveClick() {
    val current = _uiState.value
    if (current.isSaving || current.isInitialLoading) return

    val plateError =
      when {
        current.plate.isBlank() -> UiText.StringResource(R.string.vehicle_form_plate_error_required)
        !BrFormatters.isValidPlate(current.plate) ->
          UiText.StringResource(R.string.vehicle_form_plate_error_invalid)
        else -> null
      }
    val brandError =
      if (current.brand.isBlank()) UiText.StringResource(R.string.vehicle_form_brand_error) else null
    val modelError =
      if (current.model.isBlank()) UiText.StringResource(R.string.vehicle_form_model_error) else null
    val yearError =
      if (current.year == null || current.year < 1950) {
        UiText.StringResource(R.string.vehicle_form_year_error)
      } else {
        null
      }
    val parsedMileage =
      if (current.mileageText.isBlank()) {
        null
      } else {
        current.mileageText.toIntOrNull()
      }
    val mileageError =
      if (current.mileageText.isNotBlank() && (parsedMileage == null || parsedMileage < 0)) {
        UiText.StringResource(R.string.vehicle_form_mileage_error)
      } else {
        null
      }

    val firstInvalidTarget =
      when {
        plateError != null -> VehicleFormFocusTarget.PLATE
        brandError != null -> VehicleFormFocusTarget.BRAND
        modelError != null -> VehicleFormFocusTarget.MODEL
        yearError != null -> VehicleFormFocusTarget.YEAR
        mileageError != null -> VehicleFormFocusTarget.MILEAGE
        else -> null
      }

    if (firstInvalidTarget != null) {
      _uiState.update {
        it.copy(
          plateError = plateError,
          brandError = brandError,
          modelError = modelError,
          yearError = yearError,
          mileageError = mileageError,
          isMoreDetailsExpanded = it.isMoreDetailsExpanded || mileageError != null,
        )
      }
      viewModelScope.launch {
        _events.send(VehicleFormEvent.FocusField(firstInvalidTarget))
      }
      return
    }

    _uiState.update { it.copy(isSaving = true, saveError = null) }

    viewModelScope.launch {
      val vehicle =
        Vehicle(
          id = current.vehicleId.orEmpty(),
          plate = BrFormatters.normalizePlate(current.plate),
          type = current.type,
          brand = current.brand.trim(),
          model = current.model.trim(),
          year = current.year!!,
          color = current.color.trim().takeIf { it.isNotEmpty() },
          nickname = current.nickname.trim().takeIf { it.isNotEmpty() },
          mileageKm = parsedMileage,
          isPrimary = current.isPrimary,
        )

      val result =
        if (current.isEditMode) {
          vehicleRepository.updateVehicle(vehicle)
        } else {
          vehicleRepository.addVehicle(vehicle)
        }

      when (result) {
        is AppResult.Success -> {
          val saved = result.data
          _uiState.update { it.copy(isSaving = false, hasUnsavedChanges = false) }
          val messageRes =
            if (current.isEditMode) {
              R.string.vehicle_form_updated_snackbar
            } else {
              R.string.vehicle_form_saved_snackbar
            }
          _events.send(
            VehicleFormEvent.Saved(
              UiText.StringResource(messageRes, BrFormatters.formatPlate(saved.plate)),
            ),
          )
        }
        is AppResult.Error -> {
          val isPlateValidation = result.error is AppError.Validation
          _uiState.update {
            it.copy(
              isSaving = false,
              plateError = if (isPlateValidation && !it.isEditMode) result.error.userMessage else it.plateError,
              saveError = if (!isPlateValidation || it.isEditMode) result.error.userMessage else null,
            )
          }
          if (isPlateValidation && !current.isEditMode) {
            _events.send(VehicleFormEvent.FocusField(VehicleFormFocusTarget.PLATE))
          }
        }
        AppResult.Loading -> Unit
      }
    }
  }

  private fun updateState(transform: (VehicleFormUiState) -> VehicleFormUiState) {
    _uiState.update { prev ->
      val next = transform(prev)
      val dirty = hasChangesFromSnapshot(next)
      val updated = next.copy(hasUnsavedChanges = dirty)
      persistDraft(key = updated.vehicleId ?: NEW_VEHICLE_KEY, state = updated)
      updated
    }
  }

  private fun hasChangesFromSnapshot(state: VehicleFormUiState): Boolean {
    val snapshot = initialSnapshot ?: return state.plate.isNotBlank() ||
      state.brand.isNotBlank() ||
      state.model.isNotBlank() ||
      state.year != null ||
      state.color.isNotBlank() ||
      state.nickname.isNotBlank() ||
      state.mileageText.isNotBlank()

    return state.plate != snapshot.plate ||
      state.type != snapshot.type ||
      state.brand != snapshot.brand ||
      state.model != snapshot.model ||
      state.year != snapshot.year ||
      state.color != snapshot.color ||
      state.nickname != snapshot.nickname ||
      state.mileageText != snapshot.mileageText ||
      state.isPrimary != snapshot.isPrimary
  }

  private fun persistDraft(key: String, state: VehicleFormUiState) {
    savedStateHandle[KEY_DRAFT_INITIALIZED] = true
    savedStateHandle[KEY_LOADED_VEHICLE_ID] = key
    savedStateHandle[KEY_PLATE] = state.plate
    savedStateHandle[KEY_TYPE] = state.type.name
    savedStateHandle[KEY_BRAND] = state.brand
    savedStateHandle[KEY_MODEL] = state.model
    savedStateHandle[KEY_YEAR] = state.year
    savedStateHandle[KEY_COLOR] = state.color
    savedStateHandle[KEY_NICKNAME] = state.nickname
    savedStateHandle[KEY_MILEAGE] = state.mileageText
    savedStateHandle[KEY_IS_PRIMARY] = state.isPrimary
    savedStateHandle[KEY_MORE_DETAILS] = state.isMoreDetailsExpanded
  }

  private fun restoreDraftState(): VehicleFormUiState {
    val plate = savedStateHandle.get<String>(KEY_PLATE).orEmpty()
    val typeName = savedStateHandle.get<String>(KEY_TYPE)
    val type = VehicleType.entries.firstOrNull { it.name == typeName } ?: VehicleType.CAR
    val brand = savedStateHandle.get<String>(KEY_BRAND).orEmpty()
    val model = savedStateHandle.get<String>(KEY_MODEL).orEmpty()
    val year = savedStateHandle.get<Int>(KEY_YEAR)
    val color = savedStateHandle.get<String>(KEY_COLOR).orEmpty()
    val nickname = savedStateHandle.get<String>(KEY_NICKNAME).orEmpty()
    val mileage = savedStateHandle.get<String>(KEY_MILEAGE).orEmpty()
    val isPrimary = savedStateHandle.get<Boolean>(KEY_IS_PRIMARY) ?: true
    val moreDetails = savedStateHandle.get<Boolean>(KEY_MORE_DETAILS) ?: false

    return VehicleFormUiState(
      plate = plate,
      platePattern = BrFormatters.detectPlatePattern(plate),
      type = type,
      brand = brand,
      model = model,
      year = year,
      color = color,
      nickname = nickname,
      mileageText = mileage,
      isPrimary = isPrimary,
      isMoreDetailsExpanded = moreDetails,
    )
  }

  companion object {
    const val ARG_VEHICLE_ID = "vehicleId"
    private const val NEW_VEHICLE_KEY = "__new__"
    private const val KEY_DRAFT_INITIALIZED = "draft_initialized"
    private const val KEY_LOADED_VEHICLE_ID = "loaded_vehicle_id"
    private const val KEY_PLATE = "draft_plate"
    private const val KEY_TYPE = "draft_type"
    private const val KEY_BRAND = "draft_brand"
    private const val KEY_MODEL = "draft_model"
    private const val KEY_YEAR = "draft_year"
    private const val KEY_COLOR = "draft_color"
    private const val KEY_NICKNAME = "draft_nickname"
    private const val KEY_MILEAGE = "draft_mileage"
    private const val KEY_IS_PRIMARY = "draft_is_primary"
    private const val KEY_MORE_DETAILS = "draft_more_details"
  }
}
