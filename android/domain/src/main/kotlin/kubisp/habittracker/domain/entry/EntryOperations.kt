package kubisp.habittracker.domain.entry

import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.first
import kubisp.habittracker.domain.habit.HabitId
import kubisp.habittracker.domain.habit.Measurement
import kubisp.habittracker.domain.operation.DomainLock
import kubisp.habittracker.domain.port.Clock
import kubisp.habittracker.domain.port.Entries
import kubisp.habittracker.domain.port.Habits
import kubisp.habittracker.domain.undo.CurrentUndoOffer
import kubisp.habittracker.domain.undo.UndoOffer

class EntryOperations(
    private val lock: DomainLock,
    private val habits: Habits,
    private val entries: Entries,
    private val clock: Clock,
    private val currentUndoOffer: CurrentUndoOffer
) {
    /** BHV-ENTRY-R8: one separate entry of the habit's "+" step on [date]; none on a date after today (BHV-ENTRY-R3). */
    suspend fun addPlusStep(habitId: HabitId, date: LocalDate): NumericEntryResult = lock.withLock {
        val now = clock.now()
        val timeZone = clock.timeZone()
        val moment = plusStepMoment(date, now, timeZone)
        if (isAfterNow(moment, now, timeZone)) {
            return@withLock NumericEntryResult.Rejected(setOf(NumericEntryProblem.MOMENT_AFTER_NOW))
        }
        add(Entry.Numeric(newEntryId(), habitId, numericMeasurementOf(habitId).plusStep, moment))
    }

    /** BHV-ENTRY-R8: one separate entry of the typed value at the moment of [draft]. */
    suspend fun addNumericEntry(habitId: HabitId, draft: NumericEntryDraft): NumericEntryResult = lock.withLock {
        when (val checked = checkNumericDraft(draft, stored = null, clock.now(), clock.timeZone())) {
            is CheckedNumericDraft.Invalid -> NumericEntryResult.Rejected(checked.problems)
            is CheckedNumericDraft.Valid -> add(Entry.Numeric(newEntryId(), habitId, checked.value, checked.moment))
        }
    }

    /**
     * BHV-ENTRY-R6: the value, date and time of [stored], the entry as the form was opened with it, become those of
     * [draft]; its identity stays.
     */
    suspend fun editNumericEntry(stored: Entry.Numeric, draft: NumericEntryDraft): NumericEntryResult = lock.withLock {
        when (val checked = checkNumericDraft(draft, stored, clock.now(), clock.timeZone())) {
            is CheckedNumericDraft.Invalid -> NumericEntryResult.Rejected(checked.problems)
            is CheckedNumericDraft.Valid -> edit(stored.copy(value = checked.value, dateTime = checked.moment))
        }
    }

    /**
     * BHV-ENTRY-R6: every entry can be deleted, also one after today (BHV-ENTRY-R3, BHV-ENTRY-21c). No confirmation;
     * [entry] as the form holds it becomes the undo offer (BHV-ENTRY-R6a).
     */
    suspend fun deleteEntry(entry: Entry) {
        lock.withLock {
            entries.delete(entry.id)
            currentUndoOffer.replaceWith(UndoOffer.DeletedEntry(entry))
        }
    }

    private suspend fun add(entry: Entry.Numeric): NumericEntryResult {
        entries.add(entry)
        currentUndoOffer.withdrawIfConflictingWith(entry)
        return NumericEntryResult.Saved(entry)
    }

    private suspend fun edit(entry: Entry.Numeric): NumericEntryResult {
        entries.edit(entry)
        return NumericEntryResult.Saved(entry)
    }

    private suspend fun numericMeasurementOf(habitId: HabitId): Measurement.Numeric {
        val habit = checkNotNull(habits.habit(habitId).first()) { "No habit $habitId" }
        return when (val measurement = habit.measurement) {
            is Measurement.Numeric -> measurement
        }
    }

    // Domain creates the identity of an entity with it: a random UUID (Decision 91).
    private fun newEntryId() = EntryId(UUID.randomUUID())
}
