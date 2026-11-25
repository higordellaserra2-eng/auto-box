package com.example.core.common.text

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalContext

@Immutable
sealed interface UiText {
  data class DynamicString(val value: String) : UiText

  class StringResource(
    @StringRes val resId: Int,
    vararg val args: Any,
  ) : UiText {
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is StringResource) return false
      return resId == other.resId && args.contentEquals(other.args)
    }

    override fun hashCode(): Int = 31 * resId + args.contentHashCode()
  }

  class PluralResource(
    @PluralsRes val resId: Int,
    val quantity: Int,
    vararg val args: Any,
  ) : UiText {
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is PluralResource) return false
      return resId == other.resId && quantity == other.quantity && args.contentEquals(other.args)
    }

    override fun hashCode(): Int {
      var result = resId
      result = 31 * result + quantity
      result = 31 * result + args.contentHashCode()
      return result
    }
  }

  fun asString(context: Context): String =
    when (this) {
      is DynamicString -> value
      is StringResource -> context.getString(resId, *args)
      is PluralResource -> context.resources.getQuantityString(resId, quantity, *args)
    }

  @Composable
  fun asString(): String = asString(LocalContext.current)
}
