package com.example.core.common.format

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object BrFormatters {
  private val brLocale: Locale = Locale.Builder().setLanguage("pt").setRegion("BR").build()
  private val saoPauloZone: ZoneId = ZoneId.of("America/Sao_Paulo")

  private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy", brLocale)
  private val dateShortMonthFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd 'de' MMM, yyyy", brLocale)
  private val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", brLocale)

  private val legacyPlateRegex = Regex("^[A-Z]{3}[0-9]{4}$")
  private val mercosulPlateRegex = Regex("^[A-Z]{3}[0-9][A-Z][0-9]{2}$")

  fun formatCurrency(amountInCents: Long): String =
    formatCurrency(amountInCents / 100.0)

  fun formatCurrency(amount: Double): String =
    NumberFormat.getCurrencyInstance(brLocale).format(amount)

  fun formatPoints(points: Int): String =
    NumberFormat.getIntegerInstance(brLocale).format(points)

  fun formatDate(date: LocalDate): String = date.format(dateFormatter)

  fun formatDateReadable(date: LocalDate): String = date.format(dateShortMonthFormatter)

  fun formatDateTime(instant: Instant, zoneId: ZoneId = saoPauloZone): String =
    instant.atZone(zoneId).format(dateTimeFormatter)

  fun normalizePlate(raw: String): String =
    raw.uppercase(brLocale).replace(Regex("[^A-Z0-9]"), "").take(7)

  fun formatPlate(raw: String): String {
    val clean = normalizePlate(raw)
    return when {
      clean.length <= 3 -> clean
      mercosulPlateRegex.matches(clean) -> clean
      else -> "${clean.substring(0, 3)}-${clean.substring(3)}"
    }
  }

  fun isValidPlate(raw: String): Boolean =
    detectPlatePattern(raw) != PlatePattern.INVALID

  fun detectPlatePattern(raw: String): PlatePattern {
    val clean = normalizePlate(raw)
    return when {
      mercosulPlateRegex.matches(clean) -> PlatePattern.MERCOSUL
      legacyPlateRegex.matches(clean) -> PlatePattern.LEGACY
      else -> PlatePattern.INVALID
    }
  }

  fun formatPlateForAccessibility(raw: String): String =
    normalizePlate(raw).toCharArray().joinToString(" ")

  fun formatCpf(raw: String): String {
    val digits = raw.filter(Char::isDigit).take(11)
    return buildString {
      digits.forEachIndexed { index, ch ->
        if (index == 3 || index == 6) append('.')
        if (index == 9) append('-')
        append(ch)
      }
    }
  }

  fun maskCpfForDisplay(raw: String): String {
    val digits = raw.filter(Char::isDigit)
    if (digits.length != 11) return "***.***.***-**"
    return "***.${digits.substring(3, 6)}.${digits.substring(6, 9)}-**"
  }

  fun formatPhone(raw: String): String {
    val digits = raw.filter(Char::isDigit).take(11)
    return when {
      digits.isEmpty() -> ""
      digits.length <= 2 -> "(${digits}"
      digits.length <= 6 -> "(${digits.substring(0, 2)}) ${digits.substring(2)}"
      digits.length <= 10 ->
        "(${digits.substring(0, 2)}) ${digits.substring(2, 6)}-${digits.substring(6)}"
      else ->
        "(${digits.substring(0, 2)}) ${digits.substring(2, 7)}-${digits.substring(7)}"
    }
  }
}

enum class PlatePattern {
  LEGACY,
  MERCOSUL,
  INVALID,
}
