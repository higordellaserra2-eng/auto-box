package com.example.core.designsystem.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
  name = "Claro",
  showBackground = true,
  uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
  name = "Escuro",
  showBackground = true,
  uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
  name = "Fonte 200%",
  showBackground = true,
  fontScale = 2.0f,
  uiMode = Configuration.UI_MODE_NIGHT_NO,
)
annotation class AutoBoxComponentPreviews
