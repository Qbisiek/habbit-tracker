package kubisp.habittracker.domain.habit

import kubisp.habittracker.domain.decimal.ExactDecimal

/** The unit of a numeric habit: a label of its values, never converted (Decision 17). */
sealed interface NumericUnit {
    val defaultStep: ExactDecimal

    data class BuiltIn(val unit: BuiltInUnit) : NumericUnit {
        override val defaultStep: ExactDecimal get() = unit.defaultStep
    }

    /** REQ-S2: a text label defined by the user; its default "+" step is 1 (BHV-HABIT-R1). */
    data class Custom(val label: String) : NumericUnit {
        override val defaultStep: ExactDecimal get() = ExactDecimal.ONE
    }
}
