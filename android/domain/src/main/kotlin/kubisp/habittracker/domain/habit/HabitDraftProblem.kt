package kubisp.habittracker.domain.habit

/** A rule of BHV-HABIT-R1 that a [HabitDraft] breaks; each has its message at the field (`docs/UX.md` section 5). */
sealed interface HabitDraftProblem {
    data object NameEmpty : HabitDraftProblem

    data object CustomUnitEmpty : HabitDraftProblem

    /** A step set by the user that is empty, not a number, 0 or less, or 0 after rounding to 3 decimal places. */
    data object PlusStepNotAboveZero : HabitDraftProblem
}
