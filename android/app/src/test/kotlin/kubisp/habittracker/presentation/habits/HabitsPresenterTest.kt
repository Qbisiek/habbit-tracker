package kubisp.habittracker.presentation.habits

import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kubisp.habittracker.domain.fake.FakeStorage
import kubisp.habittracker.domain.fake.aHabit
import kubisp.habittracker.domain.fake.aNumericMeasurement
import kubisp.habittracker.domain.habit.BuiltInUnit
import kubisp.habittracker.domain.habit.NumericUnit
import kubisp.habittracker.presentation.unit.UnitLabel
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HabitsPresenterTest {
    private val storage = FakeStorage()

    @Test
    fun `before the first read of the habits the screen is loading, so the empty state does not flash`() = runTest {
        val presenter = givenPresenter()

        val state = presenter.state.value

        assertEquals(HabitsState.Loading, state)
    }

    @Test
    fun `without habits the list is empty`() = runTest {
        val presenter = givenPresenter()

        runCurrent()

        assertEquals(HabitsState.Shown(emptyList()), presenter.state.value)
    }

    @Test
    fun `BHV-HABIT-R4 the list shows the habits that are not archived, by name for the app language`() = runTest {
        val skating = aHabit(name = "Łyżwy")
        val ice = aHabit(name = "Lody")
        val archived = aHabit(name = "Archived", archivedOn = LocalDate.of(2026, 11, 9))
        listOf(skating, archived, ice).forEach { storage.saveHabit(it) }
        val presenter = givenPresenter(languageTag = "pl")

        runCurrent()

        assertEquals(listOf("Lody", "Łyżwy"), shownNames(presenter))
    }

    @Test
    fun `a row shows the name and the unit of the habit, built-in or custom`() = runTest {
        val water = aHabit(name = "Water", measurement = aNumericMeasurement(NumericUnit.BuiltIn(BuiltInUnit.LITRES)))
        val tea = aHabit(name = "Tea", measurement = aNumericMeasurement(NumericUnit.Custom("cups")))
        listOf(water, tea).forEach { storage.saveHabit(it) }
        val presenter = givenPresenter()

        runCurrent()

        val expected = listOf(
            HabitListRow(tea.id.value, "Tea", UnitLabel.Custom("cups")),
            HabitListRow(water.id.value, "Water", UnitLabel.BuiltIn("l"))
        )
        assertEquals(HabitsState.Shown(expected), presenter.state.value)
    }

    @Test
    fun `a habit created while the screen is shown appears in the list`() = runTest {
        val presenter = givenPresenter()
        runCurrent()

        storage.saveHabit(aHabit(name = "Reading"))
        runCurrent()

        assertEquals(listOf("Reading"), shownNames(presenter))
    }

    private fun TestScope.givenPresenter(languageTag: String = "en") =
        HabitsPresenter(storage, languageTag, backgroundScope)

    private fun shownNames(presenter: HabitsPresenter): List<String> =
        (presenter.state.value as HabitsState.Shown).habits.map { it.name }
}
