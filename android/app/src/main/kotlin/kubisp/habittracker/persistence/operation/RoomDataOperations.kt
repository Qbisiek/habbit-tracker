package kubisp.habittracker.persistence.operation

import androidx.room.withTransaction
import kubisp.habittracker.domain.habit.Habit
import kubisp.habittracker.domain.port.DataOperations
import kubisp.habittracker.persistence.HabitTrackerDatabase
import kubisp.habittracker.persistence.habit.toRow

/** Each method runs in one database transaction, so it is all-or-nothing (Decision 59). */
class RoomDataOperations(private val database: HabitTrackerDatabase) : DataOperations {
    private val habitDao = database.habitDao()

    override suspend fun saveHabit(habit: Habit) = database.withTransaction {
        val addedOrder = habitDao.addedOrderOf(habit.id.value.toString())
        if (addedOrder == null) {
            habitDao.insert(habit.toRow())
        } else {
            habitDao.update(habit.toRow(addedOrder))
        }
    }
}
