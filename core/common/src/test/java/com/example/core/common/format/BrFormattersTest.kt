package com.example.core.common.format

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrFormattersTest {

  @Test
  fun `formatCurrency formats Brazilian reais from cents`() {
    val formatted = BrFormatters.formatCurrency(123456L).replace('\u00A0', ' ')
    assertEquals("R$ 1.234,56", formatted)
  }

  @Test
  fun `formatDate and formatDateTime format in Brazilian pattern`() {
    val date = LocalDate.of(2026, 11, 25)
    assertEquals("25/11/2026", BrFormatters.formatDate(date))

    val instant = Instant.parse("2026-11-25T17:30:00Z")
    assertEquals(
      "25/11/2026 às 14:30",
      BrFormatters.formatDateTime(instant, ZoneId.of("America/Sao_Paulo")),
    )
  }

  @Test
  fun `plate formatter validates and formats legacy and Mercosul plates`() {
    assertTrue(BrFormatters.isValidPlate("abc1234"))
    assertTrue(BrFormatters.isValidPlate("BRA2E19"))
    assertFalse(BrFormatters.isValidPlate("AB123"))

    assertEquals("ABC-1234", BrFormatters.formatPlate("abc-1234"))
    assertEquals("BRA2E19", BrFormatters.formatPlate("bra2e19"))
  }

  @Test
  fun `cpf and phone formatters apply Brazilian masks`() {
    assertEquals("123.456.789-00", BrFormatters.formatCpf("12345678900"))
    assertEquals("***.456.789-**", BrFormatters.maskCpfForDisplay("12345678900"))
    assertEquals("(11) 98765-4321", BrFormatters.formatPhone("11987654321"))
    assertEquals("(11) 3456-7890", BrFormatters.formatPhone("1134567890"))
  }
}
