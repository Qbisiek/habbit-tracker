package kubisp.habittracker.domain.undo

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kubisp.habittracker.domain.entry.Entry
import kubisp.habittracker.domain.entry.EntryOperations
import kubisp.habittracker.domain.entry.NumericEntryProblem.VALUE_NOT_ABOVE_ZERO
import kubisp.habittracker.domain.entry.NumericEntryResult.Rejected
import kubisp.habittracker.domain.entry.TODAY
import kubisp.habittracker.domain.entry.clockAt
import kubisp.habittracker.domain.entry.draft
import kubisp.habittracker.domain.entry.nov
import kubisp.habittracker.domain.entry.numericValueOfDay
import kubisp.habittracker.domain.fake.FakeStorage
import kubisp.habittracker.domain.fake.aHabit
import kubisp.habittracker.domain.fake.aNumericEntry
import kubisp.habittracker.domain.fake.aNumericMeasurement
import kubisp.habittracker.domain.fake.decimal
import kubisp.habittracker.domain.habit.HabitId
import kubisp.habittracker.domain.operation.DomainLock
import kubisp.habittracker.domain.undo.CurrentUndoOffer.Companion.OFFER_DURATION
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UndoOfDeletedEntryTest {
    private val storage = FakeStorage()
    private val lock = DomainLock()
    private val clock = clockAt(nov(11, 11, 30))
    private val water = aHabit(name = "Water", measurement = aNumericMeasurement())

    @Test
    fun `BHV-ENTRY-22a the user deletes the 0,75 l entry, it is gone at once and Undo is offered`() = runTest {
        val domain = givenDomain()
        storedEntry(value = "1.25", dateTime = nov(11, 8, 0))
        val deleted = storedEntry(value = "0.75", dateTime = nov(11, 10, 0))

        domain.entries.deleteEntry(deleted)

        assertEquals(decimal("1.25"), numericValueOfDay(entriesOf(water.id, TODAY), TODAY))
        assertEquals(UndoOffer.DeletedEntry(deleted), domain.undo.offer.value)
    }

    @Test
    fun `BHV-ENTRY-22a the offer of Undo ends a few seconds after the deletion`() = runTest {
        val domain = givenDomain()
        val deleted = storedEntry(value = "0.75", dateTime = nov(11, 10, 0))
        domain.entries.deleteEntry(deleted)

        clock.advanceBy(OFFER_DURATION.minusMillis(1))
        runCurrent()
        val offerJustBefore = domain.undo.offer.value
        clock.advanceBy(Duration.ofMillis(1))
        runCurrent()

        assertEquals(UndoOffer.DeletedEntry(deleted), offerJustBefore)
        assertNull(domain.undo.offer.value)
    }

    @Test
    fun `BHV-ENTRY-22b the user taps Undo, the 0,75 l entry is back with its value, date and time`() = runTest {
        val domain = givenDomain()
        storedEntry(value = "1.25", dateTime = nov(11, 8, 0))
        val deleted = storedEntry(value = "0.75", dateTime = nov(11, 10, 0))
        domain.entries.deleteEntry(deleted)

        domain.undo.undo()

        val entries = entriesOf(water.id, TODAY)
        assertTrue(deleted in entries)
        assertEquals(decimal("2"), numericValueOfDay(entries, TODAY))
        assertNull(domain.undo.offer.value)
    }

    @Test
    fun `BHV-ENTRY-R6a a new deletion replaces the offer, the expiry of the first does not end it`() = runTest {
        val domain = givenDomain()
        val first = storedEntry(value = "1", dateTime = nov(11, 8, 0))
        val second = storedEntry(value = "2", dateTime = nov(10, 8, 0))
        domain.entries.deleteEntry(first)
        clock.advanceBy(Duration.ofSeconds(3))

        domain.entries.deleteEntry(second)
        clock.advanceBy(OFFER_DURATION.minusSeconds(3))
        runCurrent()

        assertEquals(UndoOffer.DeletedEntry(second), domain.undo.offer.value)
    }

    @Test
    fun `BHV-ENTRY-R6a a new deletion replaces the offer without a moment of no offer`() = runTest {
        val domain = givenDomain()
        val first = storedEntry(value = "1", dateTime = nov(11, 8, 0))
        val second = storedEntry(value = "2", dateTime = nov(10, 8, 0))
        val offers = mutableListOf<UndoOffer?>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { domain.undo.offer.toList(offers) }

        domain.entries.deleteEntry(first)
        domain.entries.deleteEntry(second)

        assertEquals(listOf(null, UndoOffer.DeletedEntry(first), UndoOffer.DeletedEntry(second)), offers)
    }

    @Test
    fun `BHV-ENTRY-R6a only the last deletion can be undone`() = runTest {
        val domain = givenDomain()
        val first = storedEntry(value = "1", dateTime = nov(11, 8, 0))
        val second = storedEntry(value = "2", dateTime = nov(10, 8, 0))
        domain.entries.deleteEntry(first)
        domain.entries.deleteEntry(second)

        domain.undo.undo()

        assertEquals(listOf(second), entriesOf(water.id, nov(10)))
        assertEquals(emptyList<Entry>(), entriesOf(water.id, nov(11)))
    }

    @Test
    fun `BHV-ENTRY-R6a a typed entry of the habit on the day of the deleted entry withdraws the offer`() = runTest {
        val domain = givenDomain()
        domain.entries.deleteEntry(storedEntry(value = "1", dateTime = nov(10, 8, 0)))

        domain.entries.addNumericEntry(water.id, draft("2", nov(10), 20, 0))

        assertNull(domain.undo.offer.value)
    }

    @Test
    fun `BHV-ENTRY-R6a plus of the habit on the day of the deleted entry withdraws the offer`() = runTest {
        val domain = givenDomain()
        domain.entries.deleteEntry(storedEntry(value = "1", dateTime = nov(10, 8, 0)))

        domain.entries.addPlusStep(water.id, nov(10))

        assertNull(domain.undo.offer.value)
    }

    @Test
    fun `BHV-ENTRY-R6a an entry on another day or of another habit keeps the offer`() = runTest {
        val domain = givenDomain()
        val tea = aHabit(name = "Tea").also { storage.saveHabit(it) }
        val deleted = storedEntry(value = "1", dateTime = nov(10, 8, 0))
        domain.entries.deleteEntry(deleted)

        domain.entries.addPlusStep(water.id, TODAY)
        domain.entries.addPlusStep(tea.id, nov(10))

        assertEquals(UndoOffer.DeletedEntry(deleted), domain.undo.offer.value)
    }

    @Test
    fun `BHV-ENTRY-R6a a rejected entry on the day of the deleted entry keeps the offer`() = runTest {
        val domain = givenDomain()
        val deleted = storedEntry(value = "1", dateTime = nov(10, 8, 0))
        domain.entries.deleteEntry(deleted)

        val result = domain.entries.addNumericEntry(water.id, draft("0", nov(10), 20, 0))

        assertEquals(Rejected(setOf(VALUE_NOT_ABOVE_ZERO)), result)
        assertEquals(UndoOffer.DeletedEntry(deleted), domain.undo.offer.value)
    }

    @Test
    fun `a dropped offer is gone and Undo then brings nothing back`() = runTest {
        val domain = givenDomain()
        domain.entries.deleteEntry(storedEntry(value = "1", dateTime = nov(10, 8, 0)))

        domain.undo.dropOffer()
        domain.undo.undo()

        assertNull(domain.undo.offer.value)
        assertEquals(emptyList<Entry>(), entriesOf(water.id, nov(10)))
    }

    @Test
    fun `an expired offer is not brought back by Undo`() = runTest {
        val domain = givenDomain()
        domain.entries.deleteEntry(storedEntry(value = "1", dateTime = nov(10, 8, 0)))
        clock.advanceBy(OFFER_DURATION)
        runCurrent()

        domain.undo.undo()

        assertEquals(emptyList<Entry>(), entriesOf(water.id, nov(10)))
    }

    @Test
    fun `the expiry drops the offer only after the running operation has released the Domain lock`() = runTest {
        val domain = givenDomain()
        val deleted = storedEntry(value = "1", dateTime = nov(10, 8, 0))
        domain.entries.deleteEntry(deleted)
        val operationMayFinish = CompletableDeferred<Unit>()
        launch { lock.withLock { operationMayFinish.await() } }
        runCurrent()

        clock.advanceBy(OFFER_DURATION)
        runCurrent()
        val offerWhileLocked = domain.undo.offer.value
        operationMayFinish.complete(Unit)
        runCurrent()

        assertEquals(UndoOffer.DeletedEntry(deleted), offerWhileLocked)
        assertNull(domain.undo.offer.value)
    }

    @Test
    fun `undo and dropping the offer run under the Domain lock`() = runTest {
        val domain = givenDomain()
        val deleted = storedEntry(value = "1", dateTime = nov(10, 8, 0))
        domain.entries.deleteEntry(deleted)
        val calls = listOf<suspend () -> Unit>({ domain.undo.undo() }, { domain.undo.dropOffer() })

        val failures = calls.map { call -> runCatching { lock.withLock { call() } }.exceptionOrNull() }

        assertTrue("$failures", failures.all { it is IllegalStateException })
        assertEquals(UndoOffer.DeletedEntry(deleted), domain.undo.offer.value)
        assertEquals(emptyList<Entry>(), entriesOf(water.id, nov(10)))
    }

    private suspend fun TestScope.givenDomain(): Domain {
        storage.saveHabit(water)
        return Domain(backgroundScope)
    }

    private inner class Domain(scope: CoroutineScope) {
        private val currentUndoOffer = CurrentUndoOffer(lock, clock, scope)
        val entries = EntryOperations(lock, storage, storage, clock, currentUndoOffer)
        val undo = UndoOperations(lock, storage, currentUndoOffer)
    }

    private suspend fun storedEntry(value: String, dateTime: LocalDateTime): Entry.Numeric =
        aNumericEntry(water.id, value = decimal(value), dateTime = dateTime).also { storage.add(it) }

    private suspend fun entriesOf(habitId: HabitId, day: LocalDate): List<Entry.Numeric> =
        storage.entriesOverlapping(habitId, day..day, clock.timeZone()).first().filterIsInstance<Entry.Numeric>()
}
