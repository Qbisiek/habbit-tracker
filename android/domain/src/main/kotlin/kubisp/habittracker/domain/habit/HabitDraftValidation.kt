package kubisp.habittracker.domain.habit

import kubisp.habittracker.domain.decimal.ExactDecimal
import kubisp.habittracker.domain.decimal.parseDecimalInput

/** BHV-HABIT-R1. */
fun validateHabitDraft(draft: HabitDraft): Set<HabitDraftProblem> = when (val checked = checkHabitDraft(draft)) {
    is CheckedHabitDraft.Valid -> emptySet()
    is CheckedHabitDraft.Invalid -> checked.problems
}

internal sealed interface CheckedHabitDraft {
    /** [measurement] holds the typed "+" step parsed and rounded to 3 decimal places. */
    data class Valid(val measurement: Measurement) : CheckedHabitDraft

    data class Invalid(val problems: Set<HabitDraftProblem>) : CheckedHabitDraft
}

internal fun checkHabitDraft(draft: HabitDraft): CheckedHabitDraft {
    val problems = buildSet {
        if (draft.name.isBlank()) add(HabitDraftProblem.NameEmpty)
        addAll(measurementProblems(draft.measurement))
    }
    if (problems.isNotEmpty()) return CheckedHabitDraft.Invalid(problems)
    return CheckedHabitDraft.Valid(measurementOf(draft.measurement))
}

private fun measurementProblems(measurement: MeasurementDraft): Set<HabitDraftProblem> = when (measurement) {
    is MeasurementDraft.Numeric -> numericProblems(measurement)
}

private fun numericProblems(measurement: MeasurementDraft.Numeric): Set<HabitDraftProblem> = buildSet {
    val unit = measurement.unit
    if (unit is NumericUnit.Custom && unit.label.isBlank()) add(HabitDraftProblem.CustomUnitEmpty)
    val plusStep = measurement.plusStep
    if (plusStep != null && plusStepOf(plusStep) == null) add(HabitDraftProblem.PlusStepNotAboveZero)
}

// Called only for a draft without problems, so a typed step always has a value here.
private fun measurementOf(measurement: MeasurementDraft): Measurement = when (measurement) {
    is MeasurementDraft.Numeric -> Measurement.Numeric(measurement.unit, measurement.plusStep?.let(::plusStepOf))
}

// BHV-HABIT-R1: the step must be above 0 with the precision of 3 decimal places (input rounded by BHV-EVAL-R3).
private fun plusStepOf(text: String): ExactDecimal? {
    val typed = parseDecimalInput(text) ?: return null
    return ExactDecimal.of(typed).takeIf { it.toBigDecimal().signum() > 0 }
}
