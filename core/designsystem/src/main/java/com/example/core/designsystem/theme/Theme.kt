package com.example.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext

object AutoBoxTheme {
  val semanticColors: AutoBoxSemanticColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAutoBoxSemanticColors.current
}

@Composable
fun AutoBoxTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> AutoBoxDarkColorScheme
      else -> AutoBoxLightColorScheme
    }

  val semanticColors = if (darkTheme) DarkSemanticColors else LightSemanticColors

  CompositionLocalProvider(LocalAutoBoxSemanticColors provides semanticColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = AutoBoxTypography,
      shapes = AutoBoxShapes,
      content = content,
    )
  }
}
