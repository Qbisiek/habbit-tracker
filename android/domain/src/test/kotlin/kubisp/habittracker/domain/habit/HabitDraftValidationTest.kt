package kubisp.habittracker.domain.habit

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
        val draft = aDraft(plusStep = "0")

        val problems = validateHabitDraft(draft)

        assertEquals(setOf(HabitDraftProblem.PlusStepNotAboveZero), problems)
    }

    @Test
    fun `BHV-HABIT-R1 a plus step below the precision of 3 decimal places becomes 0 and is rejected`() {
        val draft = aDraft(plusStep = "0.0004")

        val problems = validateHabitDraft(draft)

        assertEquals(setOf(HabitDraftProblem.PlusStepNotAboveZero), problems)
    }

    @Test
    fun `BHV-HABIT-R1 a negative plus step is rejected`() {
        val draft = aDraft(plusStep = "-1")

        val problems = validateHabitDraft(draft)

        assertEquals(setOf(HabitDraftProblem.PlusStepNotAboveZero), problems)
    }

    @Test
    fun `BHV-HABIT-R1 a plus step set by the user and then emptied is rejected`() {
        val draft = aDraft(plusStep = "")

        val problems = validateHabitDraft(draft)

        assertEquals(setOf(HabitDraftProblem.PlusStepNotAboveZero), problems)
    }

    @Test
    fun `BHV-HABIT-R1 a plus step that is not a number is rejected`() {
        val drafts = listOf(aDraft(plusStep = "abc"), aDraft(plusStep = "1.2.3"), aDraft(plusStep = ","))

        val problems = drafts.map { validateHabitDraft(it) }

        assertEquals(List(3) { setOf(HabitDraftProblem.PlusStepNotAboveZero) }, problems)
    }

    @Test
    fun `BHV-HABIT-R1 a plus step of one thousandth after rounding is valid, typed with a dot or a comma`() {
        val drafts = listOf(aDraft(plusStep = "0.001"), aDraft(plusStep = "0,001"), aDraft(plusStep = "0.0005"))

        val problems = drafts.map { validateHabitDraft(it) }

        assertEquals(List(3) { emptySet<HabitDraftProblem>() }, problems)
    }

    @Test
    fun `BHV-HABIT-R1 every broken rule of a draft is reported`() {
        val draft = aDraft(name = " ", unit = NumericUnit.Custom(""), plusStep = "0")

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
        plusStep: String? = null
    ) = HabitDraft(
        name = name,
        direction = Direction.BUILD,
        measurement = MeasurementDraft.Numeric(unit = unit, plusStep = plusStep)
    )
}
