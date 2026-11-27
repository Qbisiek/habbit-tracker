package kubisp.habittracker.persistence.habit

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kubisp.habittracker.domain.habit.Habit
import kubisp.habittracker.domain.habit.HabitId
import kubisp.habittracker.domain.port.Habits

// Room emits again on every write of the table; distinctUntilChanged drops the repeats of an unchanged result.
class RoomHabits(private val dao: HabitDao) : Habits {
    override fun habits(): Flow<List<Habit>> = dao.habits()
        .map { rows -> rows.map { it.toDomain() } }
        .distinctUntilChanged()

    override fun habit(id: HabitId): Flow<Habit?> = dao.habit(id.value.toString())
        .map { row -> row?.toDomain() }
        .distinctUntilChanged()
}
