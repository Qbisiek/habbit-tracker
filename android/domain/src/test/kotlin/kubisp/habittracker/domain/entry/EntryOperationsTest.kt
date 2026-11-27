package kubisp.habittracker.domain.entry

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kubisp.habittracker.domain.entry.NumericEntryProblem.MOMENT_AFTER_NOW
import kubisp.habittracker.domain.entry.NumericEntryProblem.VALUE_BELOW_SMALLEST
import kubisp.habittracker.domain.entry.NumericEntryProblem.VALUE_NOT_ABOVE_ZERO
import kubisp.habittracker.domain.entry.NumericEntryResult.Rejected
import kubisp.habittracker.domain.entry.NumericEntryResult.Saved
import kubisp.habittracker.domain.fake.FakeStorage
import kubisp.habittracker.domain.fake.aHabit
import kubisp.habittracker.domain.fake.aNumericEntry
import kubisp.habittracker.domain.fake.aNumericMeasurement
import kubisp.habittracker.domain.fake.decimal
import kubisp.habittracker.domain.habit.HabitId
import kubisp.habittracker.domain.habit.NumericUnit
import kubisp.habittracker.domain.operation.DomainLock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EntryOperationsTest {
    private val storage = FakeStorage()
    private val lock = DomainLock()
    private var clock = clockAt(nov(11, 11, 30, 27))
    private val water = aHabit(name = "Water", measurement = aNumericMeasurement())

    @Test
    fun `BHV-ENTRY-9a the user enters 0, it is rejected`() = runTest {
        storage.saveHabit(water)

        val result = operations().addNumericEntry(water.id, draft("0", TODAY, 9, 0))

        assertEquals(Rejected(setOf(VALUE_NOT_ABOVE_ZERO)), result)
        assertEquals(emptyList<Entry>(), entriesOf(water.id, TODAY))
    }

    @Test
    fun `BHV-ENTRY-9b the user enters -1, it is rejected`() = runTest {
        storage.saveHabit(water)

        val result = operations().addNumericEntry(water.id, draft("-1", TODAY, 9, 0))

        assertEquals(Rejected(setOf(VALUE_NOT_ABOVE_ZERO)), result)
        assertEquals(emptyList<Entry>(), entriesOf(water.id, TODAY))
    }

    @Test
    fun `BHV-ENTRY-9c the user enters 0,0004, it is rejected because it rounds to 0,000`() = runTest {
        storage.saveHabit(water)

        val result = operations().addNumericEntry(water.id, draft("0.0004", TODAY, 9, 0))

        assertEquals(Rejected(setOf(VALUE_BELOW_SMALLEST)), result)
        assertEquals(emptyList<Entry>(), entriesOf(water.id, TODAY))
    }

    @Test
    fun `BHV-ENTRY-9d the user types 1,5 with a decimal comma, one entry of 1,5 l`() = runTest {
        storage.saveHabit(water)

        operations().addNumericEntry(water.id, draft("1,5", TODAY, 9, 0))

        assertEquals(listOf(decimal("1.5")), entriesOf(water.id, TODAY).map { it.value })
    }

    @Test
    fun `BHV-ENTRY-10 the user taps plus twice on one day, two entries of 0,25 l and the day is 0,5 l`() = runTest {
        storage.saveHabit(water)

        operations().addPlusStep(water.id, TODAY)
        operations().addPlusStep(water.id, TODAY)

        val entries = entriesOf(water.id, TODAY)
        assertEquals(listOf(decimal("0.25"), decimal("0.25")), entries.map { it.value })
        assertEquals(2, entries.map { it.id }.distinct().size)
        assertEquals(decimal("0.5"), numericValueOfDay(entries, TODAY))
    }

    @Test
    fun `BHV-ENTRY-11 the user types 1,5 on one day, one entry of 1,5 l`() = runTest {
        storage.saveHabit(water)

        val result = operations().addNumericEntry(water.id, draft("1.5", TODAY, 9, 0))

        val entry = entriesOf(water.id, TODAY).single()
        assertEquals(Saved(entry), result)
        assertEquals(decimal("1.5"), entry.value)
    }

    @Test
    fun `BHV-ENTRY-12a the user edits an entry of 2 l to 3 l, the day shows 3 l`() = runTest {
        storage.saveHabit(water)
        val stored = storedEntry(value = "2", dateTime = nov(10, 9, 0))

        operations().editNumericEntry(stored, draft("3", nov(10), 9, 0))

        assertEquals(decimal("3"), numericValueOfDay(entriesOf(water.id, nov(10)), nov(10)))
    }

    @Test
    fun `BHV-ENTRY-12b the user edits an entry of 2 l to 0, it is rejected and stays 2 l`() = runTest {
        storage.saveHabit(water)
        val stored = storedEntry(value = "2", dateTime = nov(10, 9, 0))

        val result = operations().editNumericEntry(stored, draft("0", nov(10), 9, 0))

        assertEquals(Rejected(setOf(VALUE_NOT_ABOVE_ZERO)), result)
        assertEquals(listOf(stored), entriesOf(water.id, nov(10)))
    }

    @Test
    fun `BHV-ENTRY-16 two entries on 10 Nov are listed by time and the day is 0,55 l`() = runTest {
        storage.saveHabit(water)

        operations().addNumericEntry(water.id, draft("0.3", nov(10), 13, 30))
        operations().addNumericEntry(water.id, draft("0.25", nov(10), 8, 0))

        val entries = entriesOf(water.id, nov(10))
        assertEquals(listOf(nov(10, 8, 0), nov(10, 13, 30)), numericEntriesByTime(entries).map { it.dateTime })
        assertEquals(decimal("0.55"), numericValueOfDay(entries, nov(10)))
    }

    @Test
    fun `BHV-ENTRY-17 the user adds an entry today at 14 00 when it is 11 30, it is rejected`() = runTest {
        storage.saveHabit(water)

        val result = operations().addNumericEntry(water.id, draft("1", TODAY, 14, 0))

        assertEquals(Rejected(setOf(MOMENT_AFTER_NOW)), result)
        assertEquals(emptyList<Entry>(), entriesOf(water.id, TODAY))
    }

    @Test
    fun `BHV-ENTRY-18 the user adds an entry for 12 Nov at any time, it is not possible`() = runTest {
        storage.saveHabit(water)

        val result = operations().addNumericEntry(water.id, draft("1", nov(12), 0, 0))

        assertEquals(Rejected(setOf(MOMENT_AFTER_NOW)), result)
        assertEquals(emptyList<Entry>(), entriesOf(water.id, nov(12)))
    }

    @Test
    fun `BHV-ENTRY-R3 plus on a date after today adds no entry`() = runTest {
        storage.saveHabit(water)

        val result = operations().addPlusStep(water.id, nov(12))

        assertEquals(Rejected(setOf(MOMENT_AFTER_NOW)), result)
        assertEquals(emptyList<Entry>(), entriesOf(water.id, nov(12)))
    }

    @Test
    fun `BHV-ENTRY-20 the user taps plus on the past date 10 Nov, an entry of 0,25 l is added on 10 Nov`() = runTest {
        storage.saveHabit(water)

        operations().addPlusStep(water.id, nov(10))

        val added = entriesOf(water.id, nov(10)).map { it.value to it.dateTime }
        assertEquals(listOf(decimal("0.25") to nov(10, 12, 0, 0)), added)
        assertEquals(emptyList<Entry>(), entriesOf(water.id, TODAY))
    }

    @Test
    fun `BHV-ENTRY-R8 plus of a habit with a custom unit adds an entry of 1 of that unit`() = runTest {
        val tea = aHabit(name = "Tea", measurement = aNumericMeasurement(unit = NumericUnit.Custom("cups")))
        storage.saveHabit(tea)

        operations().addPlusStep(tea.id, TODAY)

        assertEquals(listOf(decimal("1")), entriesOf(tea.id, TODAY).map { it.value })
    }

    @Test
    fun `BHV-ENTRY-R8 plus adds the step set by the user instead of the default of the unit`() = runTest {
        val bigSips = aHabit(measurement = aNumericMeasurement(plusStepSetByUser = decimal("0.5")))
        storage.saveHabit(bigSips)

        operations().addPlusStep(bigSips.id, TODAY)

        assertEquals(listOf(decimal("0.5")), entriesOf(bigSips.id, TODAY).map { it.value })
    }

    @Test
    fun `BHV-ENTRY-R6b plus on today's date stores the current time with its seconds`() = runTest {
        storage.saveHabit(water)

        operations().addPlusStep(water.id, TODAY)

        assertEquals(listOf(nov(11, 11, 30, 27)), entriesOf(water.id, TODAY).map { it.dateTime })
    }

    @Test
    fun `BHV-ENTRY-R6b a typed entry is stored to the minute`() = runTest {
        storage.saveHabit(water)

        operations().addNumericEntry(water.id, NumericEntryDraft("1", TODAY, nov(11, 9, 15, 42).toLocalTime()))

        assertEquals(listOf(nov(11, 9, 15, 0)), entriesOf(water.id, TODAY).map { it.dateTime })
    }

    @Test
    fun `BHV-ENTRY-R6b an edit of only the date keeps the seconds of the time`() = runTest {
        storage.saveHabit(water)
        val stored = storedEntry(value = "1", dateTime = nov(10, 8, 30, 15))

        operations().editNumericEntry(stored, draft("1", nov(9), 8, 30))

        assertEquals(listOf(nov(9, 8, 30, 15)), entriesOf(water.id, nov(9)).map { it.dateTime })
    }

    @Test
    fun `BHV-ENTRY-R6b an edit of the time gives it 0 seconds`() = runTest {
        storage.saveHabit(water)
        val stored = storedEntry(value = "1", dateTime = nov(10, 8, 30, 15))

        operations().editNumericEntry(stored, draft("1", nov(10), 8, 45))

        assertEquals(listOf(nov(10, 8, 45, 0)), entriesOf(water.id, nov(10)).map { it.dateTime })
    }

    @Test
    fun `BHV-ENTRY-R6 an edit changes the value, the date and the time of the entry and keeps its identity`() =
        runTest {
            storage.saveHabit(water)
            val stored = storedEntry(value = "1", dateTime = nov(10, 8, 30))

            val result = operations().editNumericEntry(stored, draft("0.75", nov(8), 19, 5))

            val edited = stored.copy(value = decimal("0.75"), dateTime = nov(8, 19, 5))
            assertEquals(Saved(edited), result)
            assertEquals(listOf(edited), entriesOf(water.id, nov(8)))
            assertEquals(emptyList<Entry>(), entriesOf(water.id, nov(10)))
        }

    @Test
    fun `BHV-ENTRY-R6 a deleted entry is gone and the other entries of the day stay`() = runTest {
        storage.saveHabit(water)
        val deleted = storedEntry(value = "1", dateTime = nov(10, 8, 0))
        val kept = storedEntry(value = "2", dateTime = nov(10, 9, 0))

        operations().deleteEntry(deleted)

        assertEquals(listOf(kept), entriesOf(water.id, nov(10)))
    }

    @Test
    fun `BHV-ENTRY-4 after a change of the time zone to Auckland the entry is still on 5 Nov at 23 30`() = runTest {
        storage.saveHabit(water)
        operations().addNumericEntry(water.id, draft("0.25", nov(5), 23, 30))

        clock.changeTimeZone(ZoneId.of("Pacific/Auckland"))

        val entries = entriesOf(water.id, nov(5))
        assertEquals(listOf(nov(5, 23, 30)), entries.map { it.dateTime })
        assertEquals(decimal("0.25"), numericValueOfDay(entries, nov(5)))
    }

    @Test
    fun `BHV-ENTRY-21a an entry in the future after a time zone change, editing only its value is accepted`() =
        runTest {
            val stored = givenEntryMadeInAuckland()

            val result = operations().editNumericEntry(stored, draft("0.3", nov(12), 8, 30))

            assertEquals(Saved(stored.copy(value = decimal("0.3"))), result)
            assertEquals(listOf(stored.copy(value = decimal("0.3"))), entriesOf(water.id, nov(12)))
        }

    @Test
    fun `BHV-ENTRY-21b an entry in the future, changing its time to 08 45 is rejected`() = runTest {
        val stored = givenEntryMadeInAuckland()

        val result = operations().editNumericEntry(stored, draft("1", nov(12), 8, 45))

        assertEquals(Rejected(setOf(MOMENT_AFTER_NOW)), result)
        assertEquals(listOf(stored), entriesOf(water.id, nov(12)))
    }

    @Test
    fun `BHV-ENTRY-21c an entry in the future is deleted`() = runTest {
        val stored = givenEntryMadeInAuckland()

        operations().deleteEntry(stored)

        assertEquals(emptyList<Entry>(), entriesOf(water.id, nov(12)))
    }

    @Test
    fun `BHV-ENTRY-21d an entry in the future, its time changed and set back with a new value is accepted`() = runTest {
        val stored = givenEntryMadeInAuckland()

        val problemsWhileChanged =
            numericEntryProblems(draft("0.3", nov(12), 8, 45), stored, clock.now(), clock.timeZone())
        val result = operations().editNumericEntry(stored, draft("0.3", nov(12), 8, 30))

        assertEquals(setOf(MOMENT_AFTER_NOW), problemsWhileChanged)
        assertEquals(Saved(stored.copy(value = decimal("0.3"))), result)
    }

    @Test
    fun `every entry operation runs under the Domain lock`() = runTest {
        storage.saveHabit(water)
        val stored = storedEntry(value = "1", dateTime = nov(10, 8, 0))
        val operations = operations()
        val calls = listOf<suspend () -> Unit>(
            { operations.addPlusStep(water.id, TODAY) },
            { operations.addNumericEntry(water.id, draft("1", TODAY, 9, 0)) },
            { operations.editNumericEntry(stored, draft("2", nov(10), 8, 0)) },
            { operations.deleteEntry(stored) }
        )

        val failures = calls.map { call -> runCatching { lock.withLock { call() } }.exceptionOrNull() }

        assertTrue("$failures", failures.all { it is IllegalStateException })
        assertEquals(listOf(stored), entriesOf(water.id, nov(10)))
        assertEquals(emptyList<Entry>(), entriesOf(water.id, TODAY))
    }

    private fun operations() = EntryOperations(lock = lock, habits = storage, entries = storage, clock = clock)

    private suspend fun storedEntry(value: String, dateTime: LocalDateTime): Entry.Numeric =
        aNumericEntry(water.id, value = decimal(value), dateTime = dateTime).also { storage.add(it) }

    // BHV-ENTRY-21a: made on 12 Nov 08:30 in Pacific/Auckland; the device is now in Europe/Warsaw, 11 Nov 21:00.
    private suspend fun givenEntryMadeInAuckland(): Entry.Numeric {
        clock = clockAt(nov(11, 21, 0))
        storage.saveHabit(water)
        return storedEntry(value = "1", dateTime = nov(12, 8, 30))
    }

    private suspend fun entriesOf(habitId: HabitId, day: LocalDate): List<Entry.Numeric> =
        storage.entriesOverlapping(habitId, day..day, clock.timeZone()).first().filterIsInstance<Entry.Numeric>()
}
