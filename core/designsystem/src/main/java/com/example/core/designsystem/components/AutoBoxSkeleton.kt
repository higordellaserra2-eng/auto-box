package com.example.core.designsystem.components

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.preview.AutoBoxComponentPreviews
import com.example.core.designsystem.theme.AutoBoxSpacing
import com.example.core.designsystem.theme.AutoBoxTheme

@Composable
private fun rememberAreAnimationsEnabled(): Boolean {
  val context = LocalContext.current
  return remember(context) {
    runCatching {
      Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
      ) > 0f
    }.getOrDefault(true)
  }
}

@Composable
fun ShimmerBox(
  modifier: Modifier = Modifier,
  shape: Shape = MaterialTheme.shapes.small,
) {
  val animationsEnabled = rememberAreAnimationsEnabled()
  val baseColor = MaterialTheme.colorScheme.surfaceContainerHigh
  val highlightColor = MaterialTheme.colorScheme.surfaceContainerLowest

  if (!animationsEnabled) {
    Box(
      modifier =
        modifier
          .clip(shape)
          .background(baseColor),
    )
    return
  }

  val transition = rememberInfiniteTransition(label = "autobox_shimmer")
  val translateX by
    transition.animateFloat(
      initialValue = -300f,
      targetValue = 900f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 1200, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "autobox_shimmer_offset",
    )

  val brush =
    Brush.linearGradient(
      colors = listOf(baseColor, highlightColor, baseColor),
      start = Offset(translateX, 0f),
      end = Offset(translateX + 300f, 120f),
    )

  Box(
    modifier =
      modifier
        .clip(shape)
        .background(brush),
  )
}

@Composable
fun AutoBoxCardSkeleton(
  modifier: Modifier = Modifier,
) {
  AutoBoxCard(
    modifier = modifier.fillMaxWidth(),
    variant = AutoBoxCardVariant.TONAL,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(AutoBoxSpacing.sm),
    ) {
      ShimmerBox(
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
      )
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs),
      ) {
        ShimmerBox(
          modifier =
            Modifier
              .fillMaxWidth(0.6f)
              .height(16.dp),
        )
        ShimmerBox(
          modifier =
            Modifier
              .fillMaxWidth(0.9f)
              .height(12.dp),
        )
      }
    }
    Spacer(modifier = Modifier.height(AutoBoxSpacing.md))
    ShimmerBox(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(14.dp),
    )
  }
}

@AutoBoxComponentPreviews
@Composable
private fun AutoBoxSkeletonPreview() {
  AutoBoxTheme {
    Column(
      modifier = Modifier.padding(AutoBoxSpacing.md),
      verticalArrangement = Arrangement.spacedBy(AutoBoxSpacing.sm),
    ) {
      AutoBoxCardSkeleton()
      AutoBoxCardSkeleton()
    }
  }
}
