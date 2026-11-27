package kubisp.habittracker.domain.decimal

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class DecimalInputTest {
    @Test
    fun `BHV-ENTRY-R5 a comma and a dot are both decimal separators`() {
        val inputs = listOf("1,5", "1.5", "0,25", ",5", "5.")

        val values = inputs.map { parseDecimalInput(it) }

        assertEquals(listOf("1.5", "1.5", "0.25", "0.5", "5").map { BigDecimal(it) }, values)
    }

    @Test
    fun `a negative number is parsed, so the rule above 0 can reject it`() {
        val value = parseDecimalInput("-1")

        assertEquals(BigDecimal("-1"), value)
    }

    @Test
    fun `the typed number is not rounded, so a value that rounds to 0 can be told from 0`() {
        val value = parseDecimalInput("0,0004")

        assertEquals(BigDecimal("0.0004"), value)
    }

    @Test
    fun `text that is not a plain decimal number is no value`() {
        val inputs = listOf("", " ", ",", "-", "1,2,3", "1.2,3", "1e3", "1 000", "abc", "+1")

        val values = inputs.map { parseDecimalInput(it) }

        assertEquals(inputs.map { null }, values)
    }
}
