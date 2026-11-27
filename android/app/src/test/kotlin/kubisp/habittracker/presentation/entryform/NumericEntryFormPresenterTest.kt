package kubisp.habittracker.presentation.entryform

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kubisp.habittracker.domain.entry.Entry
import kubisp.habittracker.domain.entry.EntryOperations
import kubisp.habittracker.domain.fake.FakeClock
import kubisp.habittracker.domain.fake.FakeStorage
import kubisp.habittracker.domain.fake.aHabit
import kubisp.habittracker.domain.fake.decimal
import kubisp.habittracker.domain.habit.Habit
import kubisp.habittracker.domain.operation.DomainLock
import kubisp.habittracker.domain.undo.CurrentUndoOffer
import kubisp.habittracker.presentation.unit.UnitLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NumericEntryFormPresenterTest {
    private val storage = FakeStorage()

    // 11 Nov 2026, 10:00:42 in Europe/Warsaw (UTC+1).
    private val clock = FakeClock(start = Instant.parse("2026-11-11T09:00:42Z"))
    private val today = LocalDate.of(2026, 11, 11)
    private val water = aHabit(name = "Water")

    @Test
    fun `the form is closed until it is opened`() = runTest {
        val form = givenForm()

        assertNull(form.state.value)
    }

    @Test
    fun `a new form opens with an empty value and Save disabled`() = runTest {
        val form = givenForm()

        form.openNew(water.id, "Water", UnitLabel.BuiltIn("l"), today)

        assertEquals(
            NumericEntryFormState("Water", UnitLabel.BuiltIn("l"), "", isSaveEnabled = false),
            form.state.value
        )
    }

    @Test
    fun `BHV-ENTRY-R5 a value above 0 enables Save, with a comma or a dot`() = runTest {
        val form = givenOpenForm()

        form.onAction(NumericEntryFormAction.ValueChanged("0,5"))
        val withComma = form.state.value?.isSaveEnabled
        form.onAction(NumericEntryFormAction.ValueChanged("0.5"))
        val withDot = form.state.value?.isSaveEnabled

        assertEquals(listOf(true, true), listOf(withComma, withDot))
    }

    @Test
    fun `BHV-ENTRY-9a a value of 0 keeps Save disabled`() = runTest {
        val form = givenOpenForm()

        form.onAction(NumericEntryFormAction.ValueChanged("0"))

        assertFalse(form.state.value?.isSaveEnabled ?: true)
    }

    @Test
    fun `BHV-ENTRY-9c a value that rounds to 0 keeps Save disabled`() = runTest {
        val form = givenOpenForm()

        form.onAction(NumericEntryFormAction.ValueChanged("0.0004"))

        assertFalse(form.state.value?.isSaveEnabled ?: true)
    }

    @Test
    fun `an edit that the input filter does not allow is not taken`() = runTest {
        val form = givenOpenForm()
        form.onAction(NumericEntryFormAction.ValueChanged("1"))

        form.onAction(NumericEntryFormAction.ValueChanged("-1"))

        assertEquals("1", form.state.value?.value)
    }

    @Test
    fun `BHV-ENTRY-R8 Save adds one entry of the typed value at now cut to the minute on today's date`() = runTest {
        val form = givenOpenForm(date = today)
        form.onAction(NumericEntryFormAction.ValueChanged("1,5"))

        form.onAction(NumericEntryFormAction.Save)
        runCurrent()

        assertEquals(listOf(decimal("1.5") to LocalDateTime.of(2026, 11, 11, 10, 0)), storedEntries())
        assertNull(form.state.value)
    }

    @Test
    fun `REQ-M5 Save on a past date adds the entry on that date at 12 00`() = runTest {
        val form = givenOpenForm(date = LocalDate.of(2026, 11, 5))
        form.onAction(NumericEntryFormAction.ValueChanged("2"))

        form.onAction(NumericEntryFormAction.Save)
        runCurrent()

        assertEquals(listOf(decimal("2") to LocalDateTime.of(2026, 11, 5, 12, 0)), storedEntries())
    }

    @Test
    fun `a form opened before midnight and saved after it adds the entry at the moment of opening`() = runTest {
        clock.setNow(Instant.parse("2026-11-11T22:59:30Z"))
        val form = givenOpenForm(date = today)
        form.onAction(NumericEntryFormAction.ValueChanged("1"))
        clock.setNow(Instant.parse("2026-11-11T23:01:00Z"))

        form.onAction(NumericEntryFormAction.Save)
        runCurrent()

        assertEquals(listOf(decimal("1") to LocalDateTime.of(2026, 11, 11, 23, 59)), storedEntries())
    }

    @Test
    fun `Save with Save disabled adds nothing and keeps the form open`() = runTest {
        val form = givenOpenForm()
        form.onAction(NumericEntryFormAction.ValueChanged("0"))

        form.onAction(NumericEntryFormAction.Save)
        runCurrent()

        assertEquals(emptyList<Any>(), storedEntries())
        assertTrue(form.state.value != null)
    }

    @Test
    fun `a double Save adds one entry`() = runTest {
        val form = givenOpenForm()
        form.onAction(NumericEntryFormAction.ValueChanged("1"))

        form.onAction(NumericEntryFormAction.Save)
        form.onAction(NumericEntryFormAction.Save)
        runCurrent()

        assertEquals(1, storedEntries().size)
    }

    @Test
    fun `Close closes the form without adding an entry`() = runTest {
        val form = givenOpenForm()
        form.onAction(NumericEntryFormAction.ValueChanged("1"))

        form.onAction(NumericEntryFormAction.Close)
        runCurrent()

        assertNull(form.state.value)
        assertEquals(emptyList<Any>(), storedEntries())
    }

    @Test
    fun `a form opened again starts with an empty value`() = runTest {
        val form = givenOpenForm()
        form.onAction(NumericEntryFormAction.ValueChanged("1"))
        form.onAction(NumericEntryFormAction.Close)

        form.openNew(water.id, "Water", UnitLabel.BuiltIn("l"), today)

        assertEquals("", form.state.value?.value)
    }

    private suspend fun TestScope.givenOpenForm(date: LocalDate = today): NumericEntryFormPresenter {
        storage.saveHabit(water)
        return givenForm().also { it.openNew(water.id, water.name, UnitLabel.BuiltIn("l"), date) }
    }

    private fun TestScope.givenForm(): NumericEntryFormPresenter {
        val lock = DomainLock()
        val operations = EntryOperations(lock, storage, storage, clock, CurrentUndoOffer(lock, clock, backgroundScope))
        return NumericEntryFormPresenter(operations, clock, backgroundScope)
    }

    private suspend fun storedEntries(habit: Habit = water) = storage
        .entriesOverlapping(habit.id, LocalDate.of(2026, 1, 1)..LocalDate.of(2026, 12, 31), ZoneId.of("Europe/Warsaw"))
        .first()
        .filterIsInstance<Entry.Numeric>()
        .map { it.value to it.dateTime }
}
