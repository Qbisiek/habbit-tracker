package kubisp.habittracker.domain.port

import kotlinx.coroutines.flow.Flow
import kubisp.habittracker.domain.habit.Habit
import kubisp.habittracker.domain.habit.HabitId

/** Reads of habits (`docs/DESIGN.md` section 3). Every write of a habit is a [DataOperations] method (Decision 59). */
interface Habits {
    /** All habits, archived ones too, in the order of addition (`docs/DESIGN.md` section 4.6). */
    fun habits(): Flow<List<Habit>>

    /** Emits null while no habit has this identity, e.g. after it was deleted. */
    fun habit(id: HabitId): Flow<Habit?>
}
