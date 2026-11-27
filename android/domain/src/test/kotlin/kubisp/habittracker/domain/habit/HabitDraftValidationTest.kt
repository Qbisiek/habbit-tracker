package kubisp.habittracker.domain.habit

import kubisp.habittracker.domain.decimal.ExactDecimal
import kubisp.habittracker.domain.fake.aNumericMeasurement
import kubisp.habittracker.domain.fake.decimal
import org.junit.Assert.assertEquals
import org.junit.Test

class HabitDraftValidationTest {
    @Test
    fun `BHV-HABIT-R1 a numeric draft with a name, a built-in unit and no step set by the user is valid`() {
        val draft = aDraft()

        val problems = validateHabitDraft(draft)

        assertEquals(emptySet<HabitDraftProblem>(), problems)
    }

    @Test
    fun `BHV-HABIT-1 a name of only spaces is rejected`() {
        val draft = aDraft(name = "   ")

        val problems = validateHabitDraft(draft)

        assertEquals(setOf(HabitDraftProblem.NameEmpty), problems)
    }

    @Test
    fun `BHV-HABIT-R1 an empty name and a name of other white space characters are rejected`() {
        val drafts = listOf(aDraft(name = ""), aDraft(name = " \t\n "))

        val problems = drafts.map { validateHabitDraft(it) }

        assertEquals(List(2) { setOf(HabitDraftProblem.NameEmpty) }, problems)
    }

    @Test
    fun `BHV-HABIT-R1 a name with spaces around a text is valid`() {
        val draft = aDraft(name = "  Water  ")

        val problems = validateHabitDraft(draft)

        assertEquals(emptySet<HabitDraftProblem>(), problems)
    }

    @Test
    fun `BHV-HABIT-11 a custom unit of only spaces is rejected`() {
        val draft = aDraft(unit = NumericUnit.Custom("   "))

        val problems = validateHabitDraft(draft)

        assertEquals(setOf(HabitDraftProblem.CustomUnitEmpty), problems)
    }

    @Test
    fun `BHV-HABIT-R1 a custom unit with a text label is valid`() {
        val draft = aDraft(unit = NumericUnit.Custom(" cups "))

        val problems = validateHabitDraft(draft)

        assertEquals(emptySet<HabitDraftProblem>(), problems)
    }

    @Test
    fun `BHV-HABIT-12 a plus step of 0 is rejected`() {
        val draft = aDraft(plusStepSetByUser = decimal("0"))

        val problems = validateHabitDraft(draft)

        assertEquals(setOf(HabitDraftProblem.PlusStepNotAboveZero), problems)
    }

    @Test
    fun `BHV-HABIT-R1 a plus step below the precision of 3 decimal places becomes 0 and is rejected`() {
        val draft = aDraft(plusStepSetByUser = decimal("0.0004"))

        val problems = validateHabitDraft(draft)

        assertEquals(setOf(HabitDraftProblem.PlusStepNotAboveZero), problems)
    }

    @Test
    fun `BHV-HABIT-R1 a negative plus step is rejected`() {
        val draft = aDraft(plusStepSetByUser = decimal("-1"))

        val problems = validateHabitDraft(draft)

        assertEquals(setOf(HabitDraftProblem.PlusStepNotAboveZero), problems)
    }

    @Test
    fun `BHV-HABIT-R1 the smallest plus step of one thousandth is valid`() {
        val draft = aDraft(plusStepSetByUser = decimal("0.001"))

        val problems = validateHabitDraft(draft)

        assertEquals(emptySet<HabitDraftProblem>(), problems)
    }

    @Test
    fun `BHV-HABIT-R1 every broken rule of a draft is reported`() {
        val draft = aDraft(name = " ", unit = NumericUnit.Custom(""), plusStepSetByUser = decimal("0"))

        val problems = validateHabitDraft(draft)

        val expected = setOf(
            HabitDraftProblem.NameEmpty,
            HabitDraftProblem.CustomUnitEmpty,
            HabitDraftProblem.PlusStepNotAboveZero
        )
        assertEquals(expected, problems)
    }

    private fun aDraft(
        name: String = "Water",
        unit: NumericUnit = NumericUnit.BuiltIn(BuiltInUnit.LITRES),
        plusStepSetByUser: ExactDecimal? = null
    ) = HabitDraft(
        name = name,
        direction = Direction.BUILD,
        measurement = aNumericMeasurement(unit = unit, plusStepSetByUser = plusStepSetByUser)
    )
}
