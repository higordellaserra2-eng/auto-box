package com.example.core.data.repository

import com.example.core.common.result.AppResult
import com.example.core.data.model.HomeSummary
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
  fun observeHomeSummary(): Flow<AppResult<HomeSummary>>

  suspend fun refreshHomeSummary(): AppResult<HomeSummary>
}

interface UserPreferencesRepository {
  val hasCompletedOnboarding: Flow<Boolean>

  suspend fun setOnboardingCompleted(completed: Boolean)
}
