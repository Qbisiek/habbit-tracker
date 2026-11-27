package kubisp.habittracker.domain.habit

sealed interface CreateHabitResult {
    data class Created(val habit: Habit) : CreateHabitResult

    data class Rejected(val problems: Set<HabitDraftProblem>) : CreateHabitResult
}
