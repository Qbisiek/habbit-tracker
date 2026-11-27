package kubisp.habittracker.domain.port

import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kubisp.habittracker.domain.entry.Entry
import kubisp.habittracker.domain.entry.EntryId
import kubisp.habittracker.domain.habit.HabitId

/** Single-entry writes and the reads of entries by date range (`docs/DESIGN.md` section 3, Decision 54). */
interface Entries {
    /**
     * Returns every entry of the habit whose time span overlaps [days], not only the entries that start in it. The
     * days are calendar days in [timeZone], which Domain takes from the [Clock] (test G8, `docs/DESIGN.md`
     * section 8). The entries come in the order of addition, the last tie-break of BHV-ENTRY-R6b.
     */
    fun entriesOverlapping(habitId: HabitId, days: ClosedRange<LocalDate>, timeZone: ZoneId): Flow<List<Entry>>

    /** Stores a new entry with its identity; also the undo of a deletion (BHV-ENTRY-22b). Its habit must exist. */
    suspend fun add(entry: Entry)

    /** Replaces the stored entry with the same identity; does nothing when there is none. */
    suspend fun edit(entry: Entry)

    /** Does nothing when no entry has this identity. */
    suspend fun delete(id: EntryId)
}
