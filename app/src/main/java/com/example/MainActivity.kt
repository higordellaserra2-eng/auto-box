package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.designsystem.components.LoadingState
import com.example.core.designsystem.theme.AutoBoxTheme
import com.example.navigation.AutoBoxAppShell
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  private val mainViewModel: MainViewModel by viewModels()

  @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val windowSizeClass = calculateWindowSizeClass(this)
      val startState by mainViewModel.startState.collectAsStateWithLifecycle()

      AutoBoxTheme {
        when (val state = startState) {
          AppStartState.Loading -> LoadingState(skeletonCount = 3)
          is AppStartState.Ready ->
            AutoBoxAppShell(
              widthSizeClass = windowSizeClass.widthSizeClass,
              startOnOnboarding = state.showOnboarding,
            )
        }
      }
    }
  }
}
