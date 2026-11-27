package kubisp.habittracker.presentation.habitform

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kubisp.habittracker.domain.fake.FakeClock
import kubisp.habittracker.domain.fake.FakeStorage
import kubisp.habittracker.domain.fake.decimal
import kubisp.habittracker.domain.habit.BuiltInUnit
import kubisp.habittracker.domain.habit.Direction
import kubisp.habittracker.domain.habit.Habit
import kubisp.habittracker.domain.habit.HabitOperations
import kubisp.habittracker.domain.habit.Measurement
import kubisp.habittracker.domain.habit.NumericUnit
import kubisp.habittracker.domain.operation.DomainLock
import kubisp.habittracker.domain.port.DataOperations
import kubisp.habittracker.presentation.habitform.HabitFormAction.CustomUnitChanged
import kubisp.habittracker.presentation.habitform.HabitFormAction.NameChanged
import kubisp.habittracker.presentation.habitform.HabitFormAction.PlusStepChanged
import kubisp.habittracker.presentation.habitform.HabitFormAction.Save
import kubisp.habittracker.presentation.habitform.HabitFormAction.UnitChosen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HabitFormPresenterTest {
    private val storage = FakeStorage()
    private val habitOperations = HabitOperations(DomainLock(), storage, FakeClock())
    private val litres = UnitChoice.BuiltIn("l")

    @Test
    fun `a new form opens with an empty name, Save disabled and no message`() = runTest {
        val form = givenForm()

        val state = form.state.value

        assertEquals("", state.name)
        assertFalse(state.isSaveEnabled)
        assertEquals(emptySet<HabitFormMessage>(), state.messages)
    }

    @Test
    fun `the unit can be every built-in unit or a custom one`() = runTest {
        val form = givenForm()

        val choices = form.state.value.unitChoices

        val builtIn = BuiltInUnit.entries.map { UnitChoice.BuiltIn(it.id) }
        assertEquals(builtIn + UnitChoice.Custom, choices)
    }

    @Test
    fun `BHV-HABIT-R1 a form with a name and a built-in unit can be saved`() = runTest {
        val form = givenForm()

        form.onAction(NameChanged("Water"))

        assertTrue(form.state.value.isSaveEnabled)
        assertEquals(emptySet<HabitFormMessage>(), form.state.value.messages)
    }

    @Test
    fun `BHV-HABIT-1 a name of only spaces shows its message and Save is disabled`() = runTest {
        val form = givenForm()

        form.onAction(NameChanged("   "))

        assertEquals(setOf(HabitFormMessage.NAME_EMPTY), form.state.value.messages)
        assertFalse(form.state.value.isSaveEnabled)
    }

    @Test
    fun `a name typed and deleted again shows its message, because the field was touched`() = runTest {
        val form = givenForm()

        form.onAction(NameChanged("R"))
        form.onAction(NameChanged(""))

        assertEquals(setOf(HabitFormMessage.NAME_EMPTY), form.state.value.messages)
    }

    @Test
    fun `BHV-HABIT-R1 a step the user did not set shows the default of the unit and follows the unit`() = runTest {
        val form = givenForm()
        val steps = mutableListOf(form.state.value.plusStep)

        form.onAction(UnitChosen(litres))
        steps += form.state.value.plusStep
        form.onAction(UnitChosen(UnitChoice.Custom))
        steps += form.state.value.plusStep

        val expected = listOf("1", "0.25", "1").map { PlusStepField.FollowsUnit(decimal(it).toBigDecimal()) }
        assertEquals(expected, steps)
    }

    @Test
    fun `BHV-HABIT-R1 a step set by the user stays when the unit changes`() = runTest {
        val form = givenForm()
        form.onAction(PlusStepChanged("0,5"))

        form.onAction(UnitChosen(litres))

        assertEquals(PlusStepField.Typed("0,5"), form.state.value.plusStep)
    }

    @Test
    fun `BHV-HABIT-12 a step of 0 shows its message and Save is disabled`() = runTest {
        val form = givenForm()
        form.onAction(NameChanged("Water"))

        form.onAction(PlusStepChanged("0"))

        assertEquals(setOf(HabitFormMessage.PLUS_STEP_NOT_ABOVE_ZERO), form.state.value.messages)
        assertFalse(form.state.value.isSaveEnabled)
    }

    @Test
    fun `BHV-HABIT-R1 a step set by the user and emptied shows the message of the step`() = runTest {
        val form = givenForm()
        form.onAction(NameChanged("Water"))

        form.onAction(PlusStepChanged(""))

        assertEquals(PlusStepField.Typed(""), form.state.value.plusStep)
        assertEquals(setOf(HabitFormMessage.PLUS_STEP_NOT_ABOVE_ZERO), form.state.value.messages)
    }

    @Test
    fun `an edit of the step that the input filter does not allow is not taken`() = runTest {
        val form = givenForm()
        form.onAction(PlusStepChanged("1,5"))

        listOf("-1,5", "1,5,", "1,5a").forEach { form.onAction(PlusStepChanged(it)) }

        assertEquals(PlusStepField.Typed("1,5"), form.state.value.plusStep)
    }

    @Test
    fun `a custom unit without a label disables Save without a message until the label was touched`() = runTest {
        val form = givenForm()
        form.onAction(NameChanged("Tea"))

        form.onAction(UnitChosen(UnitChoice.Custom))

        assertFalse(form.state.value.isSaveEnabled)
        assertEquals(emptySet<HabitFormMessage>(), form.state.value.messages)
    }

    @Test
    fun `BHV-HABIT-11 a custom unit of only spaces shows its message`() = runTest {
        val form = givenForm()
        form.onAction(NameChanged("Tea"))
        form.onAction(UnitChosen(UnitChoice.Custom))

        form.onAction(CustomUnitChanged("  "))

        assertEquals(setOf(HabitFormMessage.CUSTOM_UNIT_EMPTY), form.state.value.messages)
        assertFalse(form.state.value.isSaveEnabled)
    }

    @Test
    fun `Save creates a numeric build habit with the unit and the typed step, then the form is closed`() = runTest {
        val form = givenForm()
        form.onAction(NameChanged(" Water "))
        form.onAction(UnitChosen(litres))
        form.onAction(PlusStepChanged("0,5"))

        form.onAction(Save)
        runCurrent()

        val habit = storage.habits().first().single()
        assertEquals("Water", habit.name)
        assertEquals(Direction.BUILD, habit.direction)
        assertEquals(Measurement.Numeric(NumericUnit.BuiltIn(BuiltInUnit.LITRES), decimal("0.5")), habit.measurement)
        assertTrue(form.state.value.isSaved)
    }

    @Test
    fun `BHV-HABIT-R1 Save without a step set by the user stores no step, so the step follows the unit`() = runTest {
        val form = givenForm()
        form.onAction(NameChanged("Tea"))
        form.onAction(UnitChosen(UnitChoice.Custom))
        form.onAction(CustomUnitChanged("cups"))

        form.onAction(Save)
        runCurrent()

        val measurement = storage.habits().first().single().measurement as Measurement.Numeric
        assertEquals(NumericUnit.Custom("cups"), measurement.unit)
        assertNull(measurement.plusStepSetByUser)
    }

    @Test
    fun `a second tap on Save while the habit is saved creates no second habit`() = runTest {
        val form = givenForm()
        form.onAction(NameChanged("Water"))

        form.onAction(Save)
        form.onAction(Save)
        runCurrent()

        assertEquals(1, storage.habits().first().size)
        assertFalse(form.state.value.isSaveEnabled)
    }

    @Test
    fun `leaving the form while the habit is saved still creates the habit`() = runTest {
        val writeMayFinish = CompletableDeferred<Unit>()
        val slowWrite = object : DataOperations {
            override suspend fun saveHabit(habit: Habit) {
                writeMayFinish.await()
                storage.saveHabit(habit)
            }
        }
        val holderScope = CoroutineScope(coroutineContext + Job())
        val form = HabitFormPresenter(HabitOperations(DomainLock(), slowWrite, FakeClock()), holderScope)
        form.onAction(NameChanged("Water"))

        form.onAction(Save)
        runCurrent()
        holderScope.cancel()
        writeMayFinish.complete(Unit)
        runCurrent()

        assertEquals(listOf("Water"), storage.habits().first().map { it.name })
    }

    private fun TestScope.givenForm() = HabitFormPresenter(habitOperations, backgroundScope)
}
