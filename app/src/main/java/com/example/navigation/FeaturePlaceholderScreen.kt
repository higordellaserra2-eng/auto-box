package com.example.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.core.designsystem.components.EmptyState
import com.example.core.designsystem.preview.AutoBoxComponentPreviews
import com.example.core.designsystem.theme.AutoBoxTheme

@Composable
fun FeatureExtensionPlaceholderScreen(
  title: String,
  description: String,
  icon: ImageVector,
  modifier: Modifier = Modifier,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    EmptyState(
      title = title,
      description = description,
      icon = icon,
      actionLabel = actionLabel,
      onAction = onAction,
    )
  }
}

internal object ExtensionIcons {
  val payment: ImageVector = Icons.Rounded.QrCode2
  val history: ImageVector = Icons.Rounded.History
  val rewards: ImageVector = Icons.Rounded.Stars
  val vehicles: ImageVector = Icons.Rounded.DirectionsCar
  val profile: ImageVector = Icons.Rounded.Person
}

@AutoBoxComponentPreviews
@Composable
private fun FeatureExtensionPlaceholderPreview() {
  AutoBoxTheme {
    FeatureExtensionPlaceholderScreen(
      title = stringResource(R.string.stub_vehicles_title),
      description = stringResource(R.string.stub_vehicles_desc),
      icon = ExtensionIcons.vehicles,
      actionLabel = stringResource(R.string.stub_action_back_home),
      onAction = {},
    )
  }
}
