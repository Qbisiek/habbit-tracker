package kubisp.habittracker.persistence.entry

import androidx.room.withTransaction
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kubisp.habittracker.domain.entry.Entry
import kubisp.habittracker.domain.entry.EntryId
import kubisp.habittracker.domain.habit.HabitId
import kubisp.habittracker.domain.port.Entries
import kubisp.habittracker.persistence.HabitTrackerDatabase

class RoomEntries(private val database: HabitTrackerDatabase) : Entries {
    private val dao = database.entryDao()

    // The day of a Numeric entry is its wall-clock date in every time zone (BHV-ENTRY-R2), so its read needs no zone;
    // the zone decides the days of the Duration entries, which come with the full schema (T3.2).
    // Room emits again on every write of the table; distinctUntilChanged drops the repeats of an unchanged result.
    override fun entriesOverlapping(
        habitId: HabitId,
        days: ClosedRange<LocalDate>,
        timeZone: ZoneId
    ): Flow<List<Entry>> =
        dao.entriesOnDays(habitId.value.toString(), days.start.toString(), days.endInclusive.toString())
            .map { rows -> rows.map { it.toDomain() } }
            .distinctUntilChanged()

    override suspend fun add(entry: Entry) {
        dao.insert(entry.toRow())
    }

    override suspend fun edit(entry: Entry) = database.withTransaction {
        val addedOrder = dao.addedOrderOf(entry.id.value.toString()) ?: return@withTransaction
        dao.update(entry.toRow(addedOrder))
    }

    override suspend fun delete(id: EntryId) {
        dao.delete(id.value.toString())
    }
}
