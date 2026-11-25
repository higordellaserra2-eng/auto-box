package com.example.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Brand Palette — AutoBox
val BrandCobaltPrimary = Color(0xFF1453B8)
val BrandCobaltLight = Color(0xFFADC6FF)
val BrandAmberSecondary = Color(0xFFC7500B)
val BrandAmberLight = Color(0xFFFFB691)
val BrandEmeraldTertiary = Color(0xFF0A7368)
val BrandEmeraldLight = Color(0xFF7AD7CA)

val AutoBoxLightColorScheme =
  lightColorScheme(
    primary = BrandCobaltPrimary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD9E2FF),
    onPrimaryContainer = Color(0xFF001945),
    secondary = BrandAmberSecondary,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBCB),
    onSecondaryContainer = Color(0xFF391100),
    tertiary = BrandEmeraldTertiary,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFA0F2E6),
    onTertiaryContainer = Color(0xFF00201C),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFF7F9FC),
    onBackground = Color(0xFF171C24),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171C24),
    surfaceVariant = Color(0xFFE6ECF5),
    onSurfaceVariant = Color(0xFF424957),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF2F5FA),
    surfaceContainer = Color(0xFFECEFF5),
    surfaceContainerHigh = Color(0xFFE4E8F0),
    surfaceContainerHighest = Color(0xFFDCE1EA),
    outline = Color(0xFF6F7787),
    outlineVariant = Color(0xFFC2C9D6),
  )

val AutoBoxDarkColorScheme =
  darkColorScheme(
    primary = BrandCobaltLight,
    onPrimary = Color(0xFF002C6F),
    primaryContainer = Color(0xFF0A3E93),
    onPrimaryContainer = Color(0xFFD9E2FF),
    secondary = BrandAmberLight,
    onSecondary = Color(0xFF5B2000),
    secondaryContainer = Color(0xFF833300),
    onSecondaryContainer = Color(0xFFFFDBCB),
    tertiary = BrandEmeraldLight,
    onTertiary = Color(0xFF003731),
    tertiaryContainer = Color(0xFF005048),
    onTertiaryContainer = Color(0xFFA0F2E6),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF0F141D),
    onBackground = Color(0xFFE3E7F0),
    surface = Color(0xFF161D28),
    onSurface = Color(0xFFE3E7F0),
    surfaceVariant = Color(0xFF283141),
    onSurfaceVariant = Color(0xFFC2C9D6),
    surfaceContainerLowest = Color(0xFF0B0F17),
    surfaceContainerLow = Color(0xFF161D28),
    surfaceContainer = Color(0xFF1C2432),
    surfaceContainerHigh = Color(0xFF252E3E),
    surfaceContainerHighest = Color(0xFF2F394B),
    outline = Color(0xFF8C94A4),
    outlineVariant = Color(0xFF424957),
  )

@Immutable
data class AutoBoxSemanticColors(
  val success: Color,
  val onSuccess: Color,
  val successContainer: Color,
  val onSuccessContainer: Color,
  val warning: Color,
  val onWarning: Color,
  val warningContainer: Color,
  val onWarningContainer: Color,
  val info: Color,
  val onInfo: Color,
  val infoContainer: Color,
  val onInfoContainer: Color,
)

val LightSemanticColors =
  AutoBoxSemanticColors(
    success = Color(0xFF0B6E4F),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFD2F5E6),
    onSuccessContainer = Color(0xFF002116),
    warning = Color(0xFF8A4B00),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFE3C2),
    onWarningContainer = Color(0xFF2D1600),
    info = Color(0xFF1453B8),
    onInfo = Color(0xFFFFFFFF),
    infoContainer = Color(0xFFD9E2FF),
    onInfoContainer = Color(0xFF001945),
  )

val DarkSemanticColors =
  AutoBoxSemanticColors(
    success = Color(0xFF6CE0B3),
    onSuccess = Color(0xFF003826),
    successContainer = Color(0xFF005139),
    onSuccessContainer = Color(0xFFD2F5E6),
    warning = Color(0xFFFFB96C),
    onWarning = Color(0xFF4B2800),
    warningContainer = Color(0xFF6A3900),
    onWarningContainer = Color(0xFFFFE3C2),
    info = Color(0xFFADC6FF),
    onInfo = Color(0xFF002C6F),
    infoContainer = Color(0xFF0A3E93),
    onInfoContainer = Color(0xFFD9E2FF),
  )

val LocalAutoBoxSemanticColors = staticCompositionLocalOf { LightSemanticColors }
