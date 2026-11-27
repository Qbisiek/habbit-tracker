package kubisp.habittracker.persistence

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kubisp.habittracker.domain.entry.Entry
import kubisp.habittracker.domain.fake.aHabit
import kubisp.habittracker.domain.fake.aNumericEntry
import kubisp.habittracker.domain.fake.decimal
import kubisp.habittracker.domain.habit.HabitId
import kubisp.habittracker.persistence.entry.RoomEntries
import kubisp.habittracker.persistence.operation.RoomDataOperations
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomEntriesTest {
    private val database = anInMemoryDatabase()
    private val entries = RoomEntries(database)
    private val warsaw = ZoneId.of("Europe/Warsaw")
    private val water = aHabit(name = "Water")
    private val walk = aHabit(name = "Walk")
    private val tenth = LocalDate.of(2026, 11, 10)

    @Before
    fun storeHabits() = runTest {
        val dataOperations = RoomDataOperations(database)
        dataOperations.saveHabit(water)
        dataOperations.saveHabit(walk)
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun `an added entry is read back unchanged with its exact value and its seconds`() = runTest {
        val entry = aNumericEntry(
            water.id,
            value = decimal("1234567.891"),
            dateTime = LocalDateTime.of(2026, 11, 10, 7, 5, 9)
        )

        entries.add(entry)

        assertEquals(listOf(entry), entriesOn(water.id, tenth))
    }

    @Test
    fun `entries are read by the days of a range both ends included only of the given habit`() = runTest {
        val beforeRange = aNumericEntry(water.id, dateTime = LocalDateTime.of(2026, 11, 9, 23, 59, 59))
        val firstDay = aNumericEntry(water.id, dateTime = LocalDateTime.of(2026, 11, 10, 0, 0))
        val lastDay = aNumericEntry(water.id, dateTime = LocalDateTime.of(2026, 11, 12, 23, 59, 59))
        val afterRange = aNumericEntry(water.id, dateTime = LocalDateTime.of(2026, 11, 13, 0, 0))
        val otherHabit = aNumericEntry(walk.id, dateTime = LocalDateTime.of(2026, 11, 11, 12, 0))
        listOf(beforeRange, firstDay, lastDay, afterRange, otherHabit).forEach { entries.add(it) }

        val read = entries.entriesOverlapping(water.id, tenth..LocalDate.of(2026, 11, 12), warsaw).first()

        assertEquals(listOf(firstDay, lastDay), read)
    }

    @Test
    fun `BHV-ENTRY-R2 the day of a numeric entry is its wall-clock date in any time zone of the read`() = runTest {
        val evening = aNumericEntry(water.id, dateTime = LocalDateTime.of(2026, 11, 10, 23, 30))
        val afterMidnight = aNumericEntry(water.id, dateTime = LocalDateTime.of(2026, 11, 11, 0, 20))
        entries.add(evening)
        entries.add(afterMidnight)
        val eleventh = LocalDate.of(2026, 11, 11)..LocalDate.of(2026, 11, 11)

        val inWarsaw = entries.entriesOverlapping(water.id, eleventh, warsaw).first()
        val inAuckland = entries.entriesOverlapping(water.id, eleventh, ZoneId.of("Pacific/Auckland")).first()

        assertEquals(listOf(afterMidnight), inWarsaw)
        assertEquals(listOf(afterMidnight), inAuckland)
    }

    @Test
    fun `entries come in the order of addition not in the order of their times`() = runTest {
        val noon = aNumericEntry(water.id, dateTime = LocalDateTime.of(2026, 11, 10, 12, 0))
        val morning = aNumericEntry(water.id, dateTime = LocalDateTime.of(2026, 11, 10, 8, 0))
        val alsoNoon = aNumericEntry(water.id, dateTime = LocalDateTime.of(2026, 11, 10, 12, 0))

        listOf(noon, morning, alsoNoon).forEach { entries.add(it) }

        assertEquals(listOf(noon, morning, alsoNoon), entriesOn(water.id, tenth))
    }

    @Test
    fun `an edit replaces the entry and keeps its place in the order of addition`() = runTest {
        val first = aNumericEntry(water.id, value = decimal("1"))
        val second = aNumericEntry(water.id, value = decimal("2"))
        entries.add(first)
        entries.add(second)
        val edited = first.copy(value = decimal("0.5"), dateTime = LocalDateTime.of(2026, 11, 10, 18, 30))

        entries.edit(edited)

        assertEquals(listOf(edited, second), entriesOn(water.id, tenth))
    }

    @Test
    fun `an edit can move the entry to another day`() = runTest {
        val entry = aNumericEntry(water.id, dateTime = LocalDateTime.of(2026, 11, 10, 12, 0))
        entries.add(entry)
        val moved = entry.copy(dateTime = LocalDateTime.of(2026, 11, 8, 12, 0))

        entries.edit(moved)

        assertEquals(emptyList<Entry>(), entriesOn(water.id, tenth))
        assertEquals(listOf(moved), entriesOn(water.id, LocalDate.of(2026, 11, 8)))
    }

    @Test
    fun `an edit of an entry that is not stored does nothing`() = runTest {
        val stored = aNumericEntry(water.id)
        entries.add(stored)

        entries.edit(aNumericEntry(water.id))

        assertEquals(listOf(stored), entriesOn(water.id, tenth))
    }

    @Test
    fun `a deleted entry is not read and its undo adds it back at the end of the order`() = runTest {
        val first = aNumericEntry(water.id, value = decimal("1"))
        val second = aNumericEntry(water.id, value = decimal("2"))
        entries.add(first)
        entries.add(second)

        entries.delete(first.id)
        val afterDelete = entriesOn(water.id, tenth)
        entries.add(first)

        assertEquals(listOf(second), afterDelete)
        assertEquals(listOf(second, first), entriesOn(water.id, tenth))
    }

    @Test
    fun `a delete of an entry that is not stored does nothing`() = runTest {
        val stored = aNumericEntry(water.id)
        entries.add(stored)

        entries.delete(aNumericEntry(water.id).id)

        assertEquals(listOf(stored), entriesOn(water.id, tenth))
    }

    @Test
    fun `an entry of a habit that is not stored is rejected`() {
        val entry = aNumericEntry(aHabit().id)

        assertThrows(SQLiteConstraintException::class.java) { runTest { entries.add(entry) } }
    }

    @Test
    fun `an entry whose identity is already stored is rejected`() {
        val entry = aNumericEntry(water.id)
        runTest { entries.add(entry) }

        assertThrows(SQLiteConstraintException::class.java) {
            runTest { entries.add(entry.copy(value = decimal("3"))) }
        }
    }

    @Test
    fun `the read follows the writes of entries in its range`() = runTest {
        val entry = aNumericEntry(water.id, value = decimal("1"))

        entries.entriesOverlapping(water.id, tenth..tenth, warsaw).test {
            assertEquals(emptyList<Entry>(), awaitItem())
            entries.add(entry)
            assertEquals(listOf(entry), awaitItem())
            entries.edit(entry.copy(value = decimal("2")))
            assertEquals(listOf(entry.copy(value = decimal("2"))), awaitItem())
            entries.delete(entry.id)
            assertEquals(emptyList<Entry>(), awaitItem())
        }
    }

    @Test
    fun `a write outside the range does not repeat an unchanged read`() = runTest {
        val inRange = aNumericEntry(water.id, value = decimal("1"))
        entries.add(inRange)

        entries.entriesOverlapping(water.id, tenth..tenth, warsaw).test {
            assertEquals(listOf(inRange), awaitItem())
            entries.add(aNumericEntry(walk.id))
            entries.add(aNumericEntry(water.id, dateTime = LocalDateTime.of(2026, 11, 11, 9, 0)))
            entries.delete(inRange.id)
            assertEquals(emptyList<Entry>(), awaitItem())
        }
    }

    private suspend fun entriesOn(habitId: HabitId, day: LocalDate): List<Entry> =
        entries.entriesOverlapping(habitId, day..day, warsaw).first()
}
