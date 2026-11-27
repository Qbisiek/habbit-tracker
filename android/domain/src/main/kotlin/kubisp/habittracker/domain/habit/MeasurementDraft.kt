package kubisp.habittracker.domain.habit

/** The [Measurement] of a [HabitDraft] as entered; the Binary and Duration variants come with the full model (T3.1). */
sealed interface MeasurementDraft {
    /**
     * [plusStep] is the text the user typed, or null when the user did not set a step, so the step follows the unit
     * (BHV-HABIT-R1). An empty text is a step the user set and emptied, not an unset one.
     */
    data class Numeric(val unit: NumericUnit, val plusStep: String?) : MeasurementDraft
}
