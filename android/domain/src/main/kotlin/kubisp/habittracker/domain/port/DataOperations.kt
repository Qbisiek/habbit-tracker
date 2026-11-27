package kubisp.habittracker.domain.port

import kubisp.habittracker.domain.habit.Habit

/**
 * Composite writes named after business actions, each all-or-nothing (Decision 59). Domain computes the resulting
 * entities; a method only writes them. The full list is in `docs/DESIGN.md` section 3 (Ports); the others come with
 * the tasks that need them.
 */
interface DataOperations {
    /** A habit with a new identity is added at the end of the order of addition; otherwise the stored one is replaced. */
    suspend fun saveHabit(habit: Habit)
}
