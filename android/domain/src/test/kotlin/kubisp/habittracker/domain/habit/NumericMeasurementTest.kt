package kubisp.habittracker.domain.habit

import java.math.BigDecimal
import kubisp.habittracker.domain.decimal.ExactDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class NumericMeasurementTest {
    @Test
    fun `REQ-M7 every built-in unit has its identifier and default plus step`() {
        val expected = mapOf(
            "ml" to "250",
            "l" to "0.25",
            "glasses" to "1",
            "g" to "100",
            "kg" to "0.5",
            "km" to "1",
            "steps" to "1000",
            "kcal" to "100",
            "pages" to "1",
            "repetitions" to "1",
            "count" to "1"
        ).mapValues { (_, step) -> decimal(step) }

        val actual = BuiltInUnit.entries.associate { unit -> unit.id to numeric(NumericUnit.BuiltIn(unit)).plusStep }

        assertEquals(expected, actual)
    }

    @Test
    fun `BHV-HABIT-14 a custom unit without a step set by the user has the plus step 1`() {
        val measurement = numeric(NumericUnit.Custom("cups"))

        val plusStep = measurement.plusStep

        assertEquals(decimal("1"), plusStep)
    }

    @Test
    fun `a plus step set by the user replaces the default step of the unit`() {
        val measurement = numeric(NumericUnit.BuiltIn(BuiltInUnit.LITRES), plusStepSetByUser = decimal("0.5"))

        val plusStep = measurement.plusStep

        assertEquals(decimal("0.5"), plusStep)
    }

    private fun numeric(unit: NumericUnit, plusStepSetByUser: ExactDecimal? = null) =
        Measurement.Numeric(unit = unit, plusStepSetByUser = plusStepSetByUser)

    private fun decimal(value: String) = ExactDecimal.of(BigDecimal(value))
}
