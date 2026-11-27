package kubisp.habittracker.domain.decimal

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExactDecimalTest {
    @Test
    fun `Decision 46 a value has 3 decimal places`() {
        val value = ExactDecimal.of(BigDecimal("2.5"))

        val decimal = value.toBigDecimal()

        assertEquals(BigDecimal("2.500"), decimal)
    }

    @Test
    fun `BHV-EVAL-R3 a value with more than 3 decimal places is rounded half up`() {
        val roundedUp = ExactDecimal.of(BigDecimal("0.0005"))
        val roundedDownToZero = ExactDecimal.of(BigDecimal("0.0004"))
        val roundedDown = ExactDecimal.of(BigDecimal("1.2344"))

        val decimals = listOf(roundedUp, roundedDownToZero, roundedDown).map { it.toBigDecimal() }

        assertEquals(listOf(BigDecimal("0.001"), BigDecimal("0.000"), BigDecimal("1.234")), decimals)
    }

    @Test
    fun `values written with a different number of decimal places are equal`() {
        val short = ExactDecimal.of(BigDecimal("0.25"))
        val long = ExactDecimal.of(BigDecimal("0.250000"))

        val areEqual = short == long

        assertTrue(areEqual)
    }

    @Test
    fun `the sum of 0,7 and 0,1 is exactly 0,8`() {
        val first = ExactDecimal.of(BigDecimal("0.7"))
        val second = ExactDecimal.of(BigDecimal("0.1"))

        val sum = first + second

        assertEquals(ExactDecimal.of(BigDecimal("0.8")), sum)
    }

    @Test
    fun `values compare by their amount`() {
        val smaller = ExactDecimal.of(BigDecimal("0.999"))
        val larger = ExactDecimal.of(BigDecimal("1"))

        val ordered = listOf(larger, smaller).sorted()

        assertEquals(listOf(smaller, larger), ordered)
    }
}
