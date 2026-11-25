package com.example.feature.vehicles.form

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Aplica máscara visual para placas brasileiras sem alterar o valor normalizado:
 * - Padrão antigo (AAA-9999): insere hífen após os 3 primeiros caracteres quando o 5º caractere é dígito.
 * - Padrão Mercosul (AAA9A99): exibe sem hífen (ex.: BRA2E19).
 */
class PlateVisualTransformation : VisualTransformation {
  override fun filter(text: AnnotatedString): TransformedText {
    val raw = text.text.take(7)
    val shouldInsertHyphen = raw.length >= 5 && raw[4].isDigit()

    if (!shouldInsertHyphen) {
      return TransformedText(AnnotatedString(raw), OffsetMapping.Identity)
    }

    val formatted = "${raw.substring(0, 3)}-${raw.substring(3)}"
    val offsetMapping =
      object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
          val clamped = offset.coerceIn(0, raw.length)
          return if (clamped <= 3) clamped else clamped + 1
        }

        override fun transformedToOriginal(offset: Int): Int {
          val clamped = offset.coerceIn(0, formatted.length)
          return if (clamped <= 3) clamped else (clamped - 1).coerceIn(0, raw.length)
        }
      }
    return TransformedText(AnnotatedString(formatted), offsetMapping)
  }
}
