package kubisp.habittracker.domain.habit

/** BHV-HABIT-R1. */
fun validateHabitDraft(draft: HabitDraft): Set<HabitDraftProblem> = buildSet {
    if (draft.name.isBlank()) add(HabitDraftProblem.NameEmpty)
    addAll(measurementProblems(draft.measurement))
}

private fun measurementProblems(measurement: Measurement): Set<HabitDraftProblem> = when (measurement) {
    is Measurement.Numeric -> numericProblems(measurement)
}

private fun numericProblems(measurement: Measurement.Numeric): Set<HabitDraftProblem> = buildSet {
    val unit = measurement.unit
    if (unit is NumericUnit.Custom && unit.label.isBlank()) add(HabitDraftProblem.CustomUnitEmpty)
    val plusStep = measurement.plusStepSetByUser
    if (plusStep != null && plusStep.toBigDecimal().signum() <= 0) add(HabitDraftProblem.PlusStepNotAboveZero)
}
