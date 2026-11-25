package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface AppStartState {
  data object Loading : AppStartState

  data class Ready(val showOnboarding: Boolean) : AppStartState
}

@HiltViewModel
class MainViewModel @Inject constructor(
  userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

  val startState: StateFlow<AppStartState> =
    userPreferencesRepository.hasCompletedOnboarding
      .map { completed -> AppStartState.Ready(showOnboarding = !completed) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppStartState.Loading,
      )
}
