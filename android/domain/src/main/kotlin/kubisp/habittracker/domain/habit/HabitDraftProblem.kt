package kubisp.habittracker.domain.habit

/** A rule of BHV-HABIT-R1 that a [HabitDraft] breaks; each has its message at the field (`docs/UX.md` section 5). */
sealed interface HabitDraftProblem {
    data object NameEmpty : HabitDraftProblem

    data object CustomUnitEmpty : HabitDraftProblem

    data object PlusStepNotAboveZero : HabitDraftProblem
}
