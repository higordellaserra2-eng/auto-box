package com.example.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

val AutoBoxShapes =
  Shapes(
    extraSmall = RoundedCornerShape(AutoBoxRadius.xs),
    small = RoundedCornerShape(AutoBoxRadius.sm),
    medium = RoundedCornerShape(AutoBoxRadius.md),
    large = RoundedCornerShape(AutoBoxRadius.lg),
    extraLarge = RoundedCornerShape(AutoBoxRadius.xl),
  )
