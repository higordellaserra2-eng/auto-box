package com.example.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPrefsDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "autobox_user_preferences")

@Singleton
class DataStoreUserPreferencesRepository @Inject constructor(
  @ApplicationContext private val context: Context,
) : UserPreferencesRepository {

  private object Keys {
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
  }

  override val hasCompletedOnboarding: Flow<Boolean> =
    context.userPrefsDataStore.data.map { prefs -> prefs[Keys.ONBOARDING_COMPLETED] ?: false }

  override suspend fun setOnboardingCompleted(completed: Boolean) {
    context.userPrefsDataStore.edit { prefs ->
      prefs[Keys.ONBOARDING_COMPLETED] = completed
    }
  }
}
