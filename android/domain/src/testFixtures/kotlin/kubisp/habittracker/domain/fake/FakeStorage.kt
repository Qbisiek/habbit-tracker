package kubisp.habittracker.domain.fake

import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kubisp.habittracker.domain.entry.Entry
import kubisp.habittracker.domain.entry.EntryId
import kubisp.habittracker.domain.habit.Habit
import kubisp.habittracker.domain.habit.HabitId
import kubisp.habittracker.domain.port.DataOperations
import kubisp.habittracker.domain.port.Entries
import kubisp.habittracker.domain.port.Habits

/** One in-memory store behind all storage ports, like the one database of Persistence; lists keep the order of addition. */
class FakeStorage :
    Habits,
    Entries,
    DataOperations {
    private val storedHabits = MutableStateFlow<List<Habit>>(emptyList())
    private val storedEntries = MutableStateFlow<List<Entry>>(emptyList())

    override fun habits(): Flow<List<Habit>> = storedHabits

    override fun habit(id: HabitId): Flow<Habit?> =
        storedHabits.map { habits -> habits.find { it.id == id } }.distinctUntilChanged()

    override fun entriesOverlapping(
        habitId: HabitId,
        days: ClosedRange<LocalDate>,
        timeZone: ZoneId
    ): Flow<List<Entry>> = storedEntries
        .map { entries -> entries.filter { it.habitId == habitId && it.overlaps(days) } }
        .distinctUntilChanged()

    override suspend fun add(entry: Entry) {
        require(storedHabits.value.any { it.id == entry.habitId }) { "No habit ${entry.habitId}" }
        require(storedEntries.value.none { it.id == entry.id }) { "Entry ${entry.id} is already stored" }
        storedEntries.update { it + entry }
    }

    override suspend fun edit(entry: Entry) {
        storedEntries.update { entries -> entries.map { if (it.id == entry.id) entry else it } }
    }

    override suspend fun delete(id: EntryId) {
        storedEntries.update { entries -> entries.filterNot { it.id == id } }
    }

    override suspend fun saveHabit(habit: Habit) {
        storedHabits.update { habits -> habits.withSaved(habit) }
    }

    private fun List<Habit>.withSaved(habit: Habit): List<Habit> =
        if (any { it.id == habit.id }) map { if (it.id == habit.id) habit else it } else this + habit

    // The day of a Numeric entry is its wall-clock date, in every time zone (BHV-ENTRY-R2).
    private fun Entry.overlaps(days: ClosedRange<LocalDate>): Boolean = when (this) {
        is Entry.Numeric -> dateTime.toLocalDate() in days
    }
}
