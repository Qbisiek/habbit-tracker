package kubisp.habittracker.domain.fake

import app.cash.turbine.test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeStorageTest {
    private val storage = FakeStorage()
    private val warsaw = ZoneId.of("Europe/Warsaw")

    @Test
    fun `habits come in the order of addition, archived ones too`() = runTest {
        val water = aHabit(name = "Water")
        val archived = aHabit(name = "Apples", archivedOn = LocalDate.of(2026, 11, 5))
        storage.saveHabit(water)
        storage.saveHabit(archived)

        val habits = storage.habits().first()

        assertEquals(listOf(water, archived), habits)
    }

    @Test
    fun `saving a stored habit replaces it and keeps its place in the order`() = runTest {
        val water = aHabit(name = "Water")
        val walk = aHabit(name = "Walk")
        storage.saveHabit(water)
        storage.saveHabit(walk)

        storage.saveHabit(water.copy(name = "Tea"))

        assertEquals(listOf(water.copy(name = "Tea"), walk), storage.habits().first())
    }

    @Test
    fun `a habit is read by its identity and is null before it is saved`() = runTest {
        val water = aHabit()

        storage.habit(water.id).test {
            assertNull(awaitItem())
            storage.saveHabit(water)
            assertEquals(water, awaitItem())
        }
    }

    @Test
    fun `entries are read by the days of a range, only of the given habit`() = runTest {
        val water = aHabit()
        val walk = aHabit()
        storage.saveHabit(water)
        storage.saveHabit(walk)
        val beforeRange = aNumericEntry(water.id, dateTime = LocalDateTime.of(2026, 11, 9, 23, 59, 59))
        val firstDay = aNumericEntry(water.id, dateTime = LocalDateTime.of(2026, 11, 10, 0, 0))
        val lastDay = aNumericEntry(water.id, dateTime = LocalDateTime.of(2026, 11, 11, 23, 59, 59))
        val afterRange = aNumericEntry(water.id, dateTime = LocalDateTime.of(2026, 11, 12, 0, 0))
        val otherHabit = aNumericEntry(walk.id, dateTime = LocalDateTime.of(2026, 11, 10, 12, 0))
        listOf(beforeRange, firstDay, lastDay, afterRange, otherHabit).forEach { storage.add(it) }

        val entries = storage.entriesOverlapping(water.id, days(10, 11), warsaw).first()

        assertEquals(listOf(firstDay, lastDay), entries)
    }

    @Test
    fun `a read of entries emits again after a write`() = runTest {
        val water = aHabit()
        storage.saveHabit(water)
        val entry = aNumericEntry(water.id)

        storage.entriesOverlapping(water.id, days(10, 10), warsaw).test {
            assertEquals(emptyList<Any>(), awaitItem())
            storage.add(entry)
            assertEquals(listOf(entry), awaitItem())
        }
    }

    @Test
    fun `an edit replaces the entry with the same identity, a delete removes it`() = runTest {
        val water = aHabit()
        storage.saveHabit(water)
        val edited = aNumericEntry(water.id)
        val deleted = aNumericEntry(water.id)
        storage.add(edited)
        storage.add(deleted)

        storage.edit(edited.copy(value = decimal("2.5")))
        storage.delete(deleted.id)

        assertEquals(
            listOf(edited.copy(value = decimal("2.5"))),
            storage.entriesOverlapping(water.id, days(10, 10), warsaw).first()
        )
    }

    @Test
    fun `an edit or a delete of an unknown entry changes nothing`() = runTest {
        val water = aHabit()
        storage.saveHabit(water)
        val stored = aNumericEntry(water.id)
        storage.add(stored)

        storage.edit(aNumericEntry(water.id, value = decimal("5")))
        storage.delete(aNumericEntry(water.id).id)

        assertEquals(listOf(stored), storage.entriesOverlapping(water.id, days(10, 10), warsaw).first())
    }

    @Test
    fun `an entry of an unknown habit or with a stored identity is rejected`() = runTest {
        val water = aHabit()
        storage.saveHabit(water)
        val stored = aNumericEntry(water.id)
        storage.add(stored)

        val unknownHabit = runCatching { storage.add(aNumericEntry(aHabit().id)) }.exceptionOrNull()
        val storedIdentity = runCatching { storage.add(stored.copy(value = decimal("3"))) }.exceptionOrNull()

        assertTrue("$unknownHabit", unknownHabit is IllegalArgumentException)
        assertTrue("$storedIdentity", storedIdentity is IllegalArgumentException)
    }

    private fun days(first: Int, last: Int) = LocalDate.of(2026, 11, first)..LocalDate.of(2026, 11, last)
}
