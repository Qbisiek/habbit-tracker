package kubisp.habittracker.domain.decimal

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Numeric values, "+" steps, targets and sums: exact decimals with a fixed precision of 3 decimal places
 * (Decision 46, `docs/DESIGN.md` section 5.2), never floating point. The scale is always 3, so equal amounts
 * are equal values.
 */
@JvmInline
value class ExactDecimal private constructor(private val value: BigDecimal) : Comparable<ExactDecimal> {
    operator fun plus(other: ExactDecimal): ExactDecimal = ExactDecimal(value + other.value)

    override fun compareTo(other: ExactDecimal): Int = value.compareTo(other.value)

    fun toBigDecimal(): BigDecimal = value

    companion object {
        private const val DECIMAL_PLACES = 3

        val ONE: ExactDecimal = of(BigDecimal.ONE)

        // BHV-EVAL-R3: input with more places is rounded half up (0.0005 becomes 0.001, 0.0004 becomes 0).
        fun of(value: BigDecimal): ExactDecimal = ExactDecimal(value.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP))
    }
}
