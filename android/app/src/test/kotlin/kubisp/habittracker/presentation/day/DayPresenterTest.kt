package kubisp.habittracker.presentation.day

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kubisp.habittracker.domain.entry.Entry
import kubisp.habittracker.domain.entry.EntryOperations
import kubisp.habittracker.domain.fake.FakeClock
import kubisp.habittracker.domain.fake.FakeStorage
import kubisp.habittracker.domain.fake.aHabit
import kubisp.habittracker.domain.fake.aNumericEntry
import kubisp.habittracker.domain.fake.aNumericMeasurement
import kubisp.habittracker.domain.fake.decimal
import kubisp.habittracker.domain.habit.BuiltInUnit
import kubisp.habittracker.domain.habit.Habit
import kubisp.habittracker.domain.habit.NumericUnit
import kubisp.habittracker.domain.operation.DomainLock
import kubisp.habittracker.domain.undo.CurrentUndoOffer
import kubisp.habittracker.presentation.entryform.NumericEntryFormPresenter
import kubisp.habittracker.presentation.unit.UnitLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DayPresenterTest {
    private val storage = FakeStorage()

    // 11 Nov 2026, 10:00 in Europe/Warsaw (UTC+1).
    private val clock = FakeClock(start = Instant.parse("2026-11-11T09:00:00Z"))
    private val today = LocalDate.of(2026, 11, 11)

    @Test
    fun `before the first read of the habits the screen is loading, so the empty state does not flash`() = runTest {
        val presenter = givenPresenter()

        val state = presenter.state.value

        assertEquals(DayState.Loading, state)
    }

    @Test
    fun `REQ-M6 the screen shows today by default`() = runTest {
        storage.saveHabit(aHabit())
        val presenter = givenPresenter()

        collect(presenter)

        assertEquals(today, shownDate(presenter))
    }

    @Test
    fun `without habits the screen shows the empty state of no habits`() = runTest {
        val presenter = givenPresenter()

        collect(presenter)

        assertEquals(DayHabits.NoHabits, shown(presenter).habits)
    }

    @Test
    fun `when every habit is archived the screen shows the empty state of archived habits`() = runTest {
        storage.saveHabit(aHabit(archivedOn = LocalDate.of(2026, 11, 9)))
        val presenter = givenPresenter()

        collect(presenter)

        assertEquals(DayHabits.AllArchived, shown(presenter).habits)
    }

    @Test
    fun `BHV-HABIT-R4 the rows are the habits that are not archived, by name for the app language`() = runTest {
        listOf(
            aHabit(name = "Łyżwy"),
            aHabit(name = "Archived", archivedOn = LocalDate.of(2026, 11, 9)),
            aHabit(name = "Lody")
        ).forEach { storage.saveHabit(it) }
        val presenter = givenPresenter(languageTag = "pl")

        collect(presenter)

        assertEquals(listOf("Lody", "Łyżwy"), rows(presenter).map { it.name })
    }

    @Test
    fun `BHV-ENTRY-R4 a row shows the total of the entries of the selected day, its unit and the action controls`() =
        runTest {
            val water = givenHabit(aHabit(name = "Water"))
            givenEntry(water, "0.25", LocalDateTime.of(2026, 11, 11, 8, 0))
            givenEntry(water, "0.5", LocalDateTime.of(2026, 11, 11, 9, 30))
            givenEntry(water, "1", LocalDateTime.of(2026, 11, 10, 9, 30))
            val presenter = givenPresenter()

            collect(presenter)

            val expected = DayRow(water.id.value, "Water", value("0.75"), UnitLabel.BuiltIn("l"), canAddEntries = true)
            assertEquals(listOf(expected), rows(presenter))
        }

    @Test
    fun `BHV-ENTRY-R4 a habit without entries on the selected day has the value 0`() = runTest {
        givenHabit(aHabit(measurement = aNumericMeasurement(NumericUnit.Custom("cups"))))
        val presenter = givenPresenter()

        collect(presenter)

        val row = rows(presenter).single()
        assertEquals(value("0"), row.value)
        assertEquals(UnitLabel.Custom("cups"), row.unit)
    }

    @Test
    fun `a new entry of the selected day changes the value of its row`() = runTest {
        val water = givenHabit(aHabit())
        val presenter = givenPresenter()
        collect(presenter)

        givenEntry(water, "0.25", LocalDateTime.of(2026, 11, 11, 8, 0))
        runCurrent()

        assertEquals(value("0.25"), rows(presenter).single().value)
    }

    @Test
    fun `REQ-M5 the previous arrow shows the day before with its values`() = runTest {
        val water = givenHabit(aHabit())
        givenEntry(water, "1", LocalDateTime.of(2026, 11, 10, 9, 30))
        val presenter = givenPresenter()
        collect(presenter)

        presenter.onAction(DayAction.ShowPreviousDay)
        runCurrent()

        assertEquals(LocalDate.of(2026, 11, 10), shownDate(presenter))
        assertEquals(value("1"), rows(presenter).single().value)
        assertTrue(rows(presenter).single().canAddEntries)
    }

    @Test
    fun `REQ-M5 a date picked in the calendar is shown`() = runTest {
        givenHabit(aHabit())
        val presenter = givenPresenter()
        collect(presenter)

        presenter.onAction(DayAction.PickDate(LocalDate.of(2026, 10, 3)))
        runCurrent()

        assertEquals(LocalDate.of(2026, 10, 3), shownDate(presenter))
    }

    @Test
    fun `BHV-ENTRY-2 the next day after today shows every habit at its value and no way to add entries`() = runTest {
        givenHabit(aHabit(name = "Water"))
        givenHabit(aHabit(name = "Tea"))
        val presenter = givenPresenter()
        collect(presenter)

        presenter.onAction(DayAction.ShowNextDay)
        runCurrent()

        assertEquals(LocalDate.of(2026, 11, 12), shownDate(presenter))
        assertEquals(listOf(value("0"), value("0")), rows(presenter).map { it.value })
        assertEquals(listOf(false, false), rows(presenter).map { it.canAddEntries })
    }

    @Test
    fun `BHV-ENTRY-2 a date after today picked in the calendar has no way to add entries`() = runTest {
        givenHabit(aHabit())
        val presenter = givenPresenter()
        collect(presenter)

        presenter.onAction(DayAction.PickDate(LocalDate.of(2026, 12, 24)))
        runCurrent()

        assertFalse(rows(presenter).single().canAddEntries)
    }

    @Test
    fun `BHV-DAY-1 a screen that follows today moves to the new day after midnight`() = runTest {
        givenHabit(aHabit())
        clock.setNow(atWarsaw(2026, 11, 11, 23, 59))
        val presenter = givenPresenter()
        collect(presenter)

        clock.advanceBy(Duration.ofMinutes(1))
        runCurrent()

        assertEquals(LocalDate.of(2026, 11, 12), shownDate(presenter))
    }

    @Test
    fun `BHV-DAY-2 a date picked by the user stays after midnight`() = runTest {
        givenHabit(aHabit())
        clock.setNow(atWarsaw(2026, 11, 11, 23, 59))
        val presenter = givenPresenter()
        collect(presenter)
        presenter.onAction(DayAction.PickDate(LocalDate.of(2026, 11, 5)))
        runCurrent()

        clock.advanceBy(Duration.ofMinutes(1))
        runCurrent()

        assertEquals(LocalDate.of(2026, 11, 5), shownDate(presenter))
    }

    @Test
    fun `BHV-DAY-R1 picking today's date puts the screen back to following today`() = runTest {
        givenHabit(aHabit())
        clock.setNow(atWarsaw(2026, 11, 11, 23, 59))
        val presenter = givenPresenter()
        collect(presenter)
        presenter.onAction(DayAction.PickDate(LocalDate.of(2026, 11, 5)))
        presenter.onAction(DayAction.PickDate(today))
        runCurrent()

        clock.advanceBy(Duration.ofMinutes(1))
        runCurrent()

        assertEquals(LocalDate.of(2026, 11, 12), shownDate(presenter))
    }

    @Test
    fun `BHV-DAY-R1 an arrow that comes back to today's date puts the screen back to following today`() = runTest {
        givenHabit(aHabit())
        clock.setNow(atWarsaw(2026, 11, 11, 23, 59))
        val presenter = givenPresenter()
        collect(presenter)
        presenter.onAction(DayAction.ShowPreviousDay)
        presenter.onAction(DayAction.ShowNextDay)
        runCurrent()

        clock.advanceBy(Duration.ofMinutes(1))
        runCurrent()

        assertEquals(LocalDate.of(2026, 11, 12), shownDate(presenter))
    }

    @Test
    fun `BHV-DAY-1 a screen that follows today shows the new day when it is shown again after midnight`() = runTest {
        givenHabit(aHabit())
        clock.setNow(atWarsaw(2026, 11, 11, 22, 0))
        val presenter = givenPresenter()
        val collector = collect(presenter)
        collector.cancel()
        advanceTimeBy(Duration.ofMinutes(10).toMillis())

        clock.setNow(atWarsaw(2026, 11, 12, 7, 0))
        collect(presenter)

        assertEquals(LocalDate.of(2026, 11, 12), shownDate(presenter))
    }

    @Test
    fun `a change of the time zone moves a screen that follows today to the day of the new zone`() = runTest {
        givenHabit(aHabit())
        clock.setNow(atWarsaw(2026, 11, 11, 23, 30))
        val presenter = givenPresenter()
        collect(presenter)

        clock.changeTimeZone(ZoneId.of("Asia/Tokyo"))
        runCurrent()

        assertEquals(LocalDate.of(2026, 11, 12), shownDate(presenter))
        assertTrue(rows(presenter).single().canAddEntries)
    }

    @Test
    fun `a picked date after today gets the action controls when it becomes today`() = runTest {
        givenHabit(aHabit())
        clock.setNow(atWarsaw(2026, 11, 11, 23, 59))
        val presenter = givenPresenter()
        collect(presenter)
        presenter.onAction(DayAction.ShowNextDay)
        runCurrent()

        clock.advanceBy(Duration.ofMinutes(1))
        runCurrent()

        assertTrue(rows(presenter).single().canAddEntries)
    }

    @Test
    fun `BHV-ENTRY-R8 plus adds one entry of the step of the habit on today's date at now`() = runTest {
        val water = givenHabit(aHabit(measurement = aNumericMeasurement(NumericUnit.BuiltIn(BuiltInUnit.LITRES))))
        val presenter = givenPresenter()
        collect(presenter)

        presenter.onAction(DayAction.AddPlusStep(water.id.value))
        runCurrent()

        assertEquals(value("0.25"), rows(presenter).single().value)
        assertEquals(listOf(LocalDateTime.of(2026, 11, 11, 10, 0)), storedMoments(water))
    }

    @Test
    fun `BHV-ENTRY-R8 every tap of plus adds a separate entry`() = runTest {
        val water = givenHabit(aHabit())
        val presenter = givenPresenter()
        collect(presenter)

        presenter.onAction(DayAction.AddPlusStep(water.id.value))
        presenter.onAction(DayAction.AddPlusStep(water.id.value))
        runCurrent()

        assertEquals(2, storedMoments(water).size)
        assertEquals(value("0.5"), rows(presenter).single().value)
    }

    @Test
    fun `REQ-M5 plus on a past date adds the entry on that date at 12 00`() = runTest {
        val water = givenHabit(aHabit())
        val presenter = givenPresenter()
        collect(presenter)
        presenter.onAction(DayAction.PickDate(LocalDate.of(2026, 11, 5)))
        runCurrent()

        presenter.onAction(DayAction.AddPlusStep(water.id.value))
        runCurrent()

        assertEquals(listOf(LocalDateTime.of(2026, 11, 5, 12, 0)), storedMoments(water))
        assertEquals(value("0.25"), rows(presenter).single().value)
    }

    @Test
    fun `typing a value opens the numeric entry form of the habit on the selected date`() = runTest {
        val tea = givenHabit(aHabit(name = "Tea", measurement = aNumericMeasurement(NumericUnit.Custom("cups"))))
        val presenter = givenPresenter()
        collect(presenter)

        presenter.onAction(DayAction.TypeValue(tea.id.value))

        val form = presenter.numericEntryForm.state.value
        assertEquals("Tea", form?.habitName)
        assertEquals(UnitLabel.Custom("cups"), form?.unit)
    }

    private fun TestScope.givenPresenter(languageTag: String = "en"): DayPresenter {
        val lock = DomainLock()
        val operations = EntryOperations(lock, storage, storage, clock, CurrentUndoOffer(lock, clock, backgroundScope))
        val form = NumericEntryFormPresenter(operations, clock, backgroundScope)
        return DayPresenter(storage, storage, operations, clock, form, languageTag, backgroundScope)
    }

    private fun TestScope.collect(presenter: DayPresenter): Job {
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { presenter.state.collect {} }
        runCurrent()
        return job
    }

    private suspend fun givenHabit(habit: Habit): Habit = habit.also { storage.saveHabit(it) }

    private suspend fun givenEntry(habit: Habit, value: String, dateTime: LocalDateTime) {
        storage.add(aNumericEntry(habit.id, value = decimal(value), dateTime = dateTime))
    }

    private suspend fun storedMoments(habit: Habit): List<LocalDateTime> = storage
        .entriesOverlapping(habit.id, LocalDate.of(2026, 1, 1)..LocalDate.of(2026, 12, 31), clock.timeZone())
        .first()
        .filterIsInstance<Entry.Numeric>()
        .map { it.dateTime }

    private fun shown(presenter: DayPresenter): DayState.Shown = presenter.state.value as DayState.Shown

    private fun shownDate(presenter: DayPresenter): LocalDate = shown(presenter).date

    private fun rows(presenter: DayPresenter): List<DayRow> = (shown(presenter).habits as DayHabits.Rows).rows

    private fun value(text: String) = decimal(text).toBigDecimal()

    private fun atWarsaw(year: Int, month: Int, day: Int, hour: Int, minute: Int): Instant =
        LocalDateTime.of(year, month, day, hour, minute).atZone(ZoneId.of("Europe/Warsaw")).toInstant()
}
