package com.example.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed interface OnboardingEvent {
  data object NavigateToHome : OnboardingEvent
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
  private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

  private val _events = MutableSharedFlow<OnboardingEvent>()
  val events: SharedFlow<OnboardingEvent> = _events.asSharedFlow()

  fun onFinishOnboarding() {
    viewModelScope.launch {
      userPreferencesRepository.setOnboardingCompleted(true)
      _events.emit(OnboardingEvent.NavigateToHome)
    }
  }
}
