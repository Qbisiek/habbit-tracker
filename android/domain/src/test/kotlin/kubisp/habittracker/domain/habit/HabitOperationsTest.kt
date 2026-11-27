package kubisp.habittracker.domain.habit

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kubisp.habittracker.domain.fake.FakeClock
import kubisp.habittracker.domain.fake.FakeStorage
import kubisp.habittracker.domain.fake.aNumericMeasurement
import kubisp.habittracker.domain.fake.decimal
import kubisp.habittracker.domain.operation.DomainLock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HabitOperationsTest {
    private val storage = FakeStorage()
    private val clock = FakeClock(Instant.parse("2026-11-10T08:30:15.250Z"), ZoneId.of("Europe/Warsaw"))
    private val lock = DomainLock()
    private val operations = HabitOperations(lock = lock, dataOperations = storage, clock = clock)

    @Test
    fun `BHV-HABIT-R1 a valid draft creates a not archived habit with the direction and measurement of the draft`() =
        runTest {
            val measurement = aNumericMeasurement(NumericUnit.BuiltIn(BuiltInUnit.MILLILITRES), decimal("500"))
            val draft = aDraft(name = "Water", direction = Direction.QUIT, measurement = measurement)

            val result = operations.createHabit(draft)

            val habit = createdHabit(result)
            assertEquals("Water", habit.name)
            assertEquals(Direction.QUIT, habit.direction)
            assertEquals(measurement, habit.measurement)
            assertNull(habit.archivedOn)
            assertEquals(listOf(habit), storage.habits().first())
        }

    @Test
    fun `BHV-HABIT-R1 without a step set by the user the habit stores no step and uses the default of the unit`() =
        runTest {
            val draft = aDraft(measurement = aNumericMeasurement(NumericUnit.BuiltIn(BuiltInUnit.LITRES), null))

            val result = operations.createHabit(draft)

            val measurement = createdHabit(result).measurement as Measurement.Numeric
            assertNull(measurement.plusStepSetByUser)
            assertEquals(decimal("0.25"), measurement.plusStep)
        }

    @Test
    fun `BHV-HABIT-R1 the creation day and time is now on the wall clock of the time zone of the clock`() = runTest {
        val draft = aDraft()

        val result = operations.createHabit(draft)

        assertEquals(LocalDateTime.of(2026, 11, 10, 9, 30, 15), createdHabit(result).createdAt)
    }

    @Test
    fun `BHV-HABIT-R1 the name and the custom unit are stored without the white space around them`() = runTest {
        val draft = aDraft(name = "  Tea ", measurement = aNumericMeasurement(NumericUnit.Custom(" cups  "), null))

        val result = operations.createHabit(draft)

        val habit = createdHabit(result)
        assertEquals("Tea", habit.name)
        assertEquals(NumericUnit.Custom("cups"), (habit.measurement as Measurement.Numeric).unit)
        assertEquals(listOf(habit), storage.habits().first())
    }

    @Test
    fun `BHV-HABIT-R1 two habits with the same name are both created, each with its own identity`() = runTest {
        val draft = aDraft(name = "Reading")

        val first = createdHabit(operations.createHabit(draft))
        val second = createdHabit(operations.createHabit(draft))

        assertNotEquals(first.id, second.id)
        assertEquals(listOf(first, second), storage.habits().first())
    }

    @Test
    fun `BHV-HABIT-1 a name of only spaces is rejected and the habit is not created`() = runTest {
        val draft = aDraft(name = "   ")

        val result = operations.createHabit(draft)

        assertEquals(CreateHabitResult.Rejected(setOf(HabitDraftProblem.NameEmpty)), result)
        assertTrue(storage.habits().first().isEmpty())
    }

    @Test
    fun `BHV-HABIT-11 a custom unit of only spaces is rejected and the habit is not created`() = runTest {
        val draft = aDraft(measurement = aNumericMeasurement(NumericUnit.Custom("   "), null))

        val result = operations.createHabit(draft)

        assertEquals(CreateHabitResult.Rejected(setOf(HabitDraftProblem.CustomUnitEmpty)), result)
        assertTrue(storage.habits().first().isEmpty())
    }

    @Test
    fun `BHV-HABIT-12 a plus step of 0 is rejected and the habit is not created`() = runTest {
        val draft = aDraft(measurement = aNumericMeasurement(plusStepSetByUser = decimal("0")))

        val result = operations.createHabit(draft)

        assertEquals(CreateHabitResult.Rejected(setOf(HabitDraftProblem.PlusStepNotAboveZero)), result)
        assertTrue(storage.habits().first().isEmpty())
    }

    @Test
    fun `creating a habit waits until the running Domain operation has finished`() = runTest {
        val otherOperationMayFinish = CompletableDeferred<Unit>()
        launch { lock.withLock { otherOperationMayFinish.await() } }
        runCurrent()

        val creation = async { operations.createHabit(aDraft()) }
        runCurrent()
        val storedWhileLocked = storage.habits().first()
        otherOperationMayFinish.complete(Unit)
        val created = createdHabit(creation.await())

        assertTrue(storedWhileLocked.isEmpty())
        assertEquals(listOf(created), storage.habits().first())
    }

    private fun createdHabit(result: CreateHabitResult): Habit {
        assertTrue("$result", result is CreateHabitResult.Created)
        return (result as CreateHabitResult.Created).habit
    }

    private fun aDraft(
        name: String = "Water",
        direction: Direction = Direction.BUILD,
        measurement: Measurement = aNumericMeasurement()
    ) = HabitDraft(name = name, direction = direction, measurement = measurement)
}
