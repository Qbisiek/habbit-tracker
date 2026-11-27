package kubisp.habittracker.domain.habit

import kubisp.habittracker.domain.decimal.ExactDecimal

/** How a habit is measured (Decision 10); the Binary and Duration variants come with the full model (T3.1). */
sealed interface Measurement {
    data class Numeric(val unit: NumericUnit, val plusStepSetByUser: ExactDecimal?) : Measurement {
        // BHV-HABIT-R1: a step the user did not set follows the unit, also after a change of the unit.
        val plusStep: ExactDecimal get() = plusStepSetByUser ?: unit.defaultStep
    }
}
