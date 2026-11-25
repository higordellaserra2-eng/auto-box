package com.example.feature.home

import androidx.compose.runtime.Immutable
import com.example.core.common.text.UiText
import com.example.core.data.model.HomeSummary

@Immutable
sealed interface HomeUiState {
  data object Loading : HomeUiState

  data object Empty : HomeUiState

  data class Error(val message: UiText) : HomeUiState

  data class Success(
    val summary: HomeSummary,
    val isRefreshing: Boolean = false,
  ) : HomeUiState
}
