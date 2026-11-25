package com.example.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.connectivity.ConnectivityMonitor
import com.example.core.common.result.AppResult
import com.example.core.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val homeRepository: HomeRepository,
  connectivityMonitor: ConnectivityMonitor,
) : ViewModel() {

  private val manualState = MutableStateFlow<HomeUiState?>(null)
  private val isRefreshing = MutableStateFlow(false)

  val isOffline: StateFlow<Boolean> =
    connectivityMonitor.isOnline
      .let { flow ->
        combine(flow) { values -> !values[0] }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
      )

  val uiState: StateFlow<HomeUiState> =
    combine(
      homeRepository.observeHomeSummary(),
      manualState,
      isRefreshing,
    ) { result, overrideState, refreshing ->
      overrideState ?: when (result) {
        AppResult.Loading -> HomeUiState.Loading
        is AppResult.Error -> HomeUiState.Error(result.error.userMessage)
        is AppResult.Success -> {
          val data = result.data
          if (data.primaryVehicle == null && data.nextReminder == null && data.pointsSummary.currentPoints == 0) {
            HomeUiState.Empty
          } else {
            HomeUiState.Success(summary = data, isRefreshing = refreshing)
          }
        }
      }
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = HomeUiState.Loading,
    )

  fun refresh() {
    if (isRefreshing.value) return
    viewModelScope.launch {
      isRefreshing.value = true
      when (val result = homeRepository.refreshHomeSummary()) {
        is AppResult.Error -> manualState.value = HomeUiState.Error(result.error.userMessage)
        is AppResult.Success -> manualState.value = null
        AppResult.Loading -> Unit
      }
      isRefreshing.value = false
    }
  }

  fun retry() {
    manualState.value = HomeUiState.Loading
    viewModelScope.launch {
      when (val result = homeRepository.refreshHomeSummary()) {
        is AppResult.Error -> manualState.value = HomeUiState.Error(result.error.userMessage)
        is AppResult.Success -> manualState.value = null
        AppResult.Loading -> Unit
      }
    }
  }
}
