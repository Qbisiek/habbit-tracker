package kubisp.habittracker.domain.entry

import java.time.LocalDateTime
import kubisp.habittracker.domain.entry.NumericEntryProblem.MOMENT_AFTER_NOW
import kubisp.habittracker.domain.entry.NumericEntryProblem.VALUE_BELOW_SMALLEST
import kubisp.habittracker.domain.entry.NumericEntryProblem.VALUE_NOT_ABOVE_ZERO
import kubisp.habittracker.domain.fake.aHabit
import kubisp.habittracker.domain.fake.aNumericEntry
import kubisp.habittracker.domain.fake.decimal
import org.junit.Assert.assertEquals
import org.junit.Test

class NumericEntryValidationTest {
    private val now = nov(11, 11, 30, 27).atZone(WARSAW).toInstant()
    private val stored = aNumericEntry(aHabit().id, value = decimal("2"), dateTime = nov(10, 8, 30, 15))

    @Test
    fun `a value above 0 at a past moment breaks no rule`() {
        val problems = problemsOfNew(draft("0.001", TODAY, 11, 30))

        assertEquals(emptySet<NumericEntryProblem>(), problems)
    }

    @Test
    fun `an empty value is not above 0`() {
        val problems = problemsOfNew(draft("", TODAY, 9, 0))

        assertEquals(setOf(VALUE_NOT_ABOVE_ZERO), problems)
    }

    @Test
    fun `BHV-ENTRY-9c a value above 0 that rounds to 0,000 is below the smallest value, 0 is not`() {
        val roundsToZero = problemsOfNew(draft("0.0001", TODAY, 9, 0))
        val zero = problemsOfNew(draft("0", TODAY, 9, 0))

        assertEquals(setOf(VALUE_BELOW_SMALLEST), roundsToZero)
        assertEquals(setOf(VALUE_NOT_ABOVE_ZERO), zero)
    }

    @Test
    fun `a doubled local time is compared with now at its earlier offset`() {
        val inFirstRepeatedHour = LocalDateTime.of(2026, 10, 25, 2, 30).atZone(WARSAW).withEarlierOffsetAtOverlap()
        val now = inFirstRepeatedHour.toInstant()

        val before = numericEntryProblems(draft("1", inFirstRepeatedHour.toLocalDate(), 2, 15), null, now, WARSAW)
        val after = numericEntryProblems(draft("1", inFirstRepeatedHour.toLocalDate(), 2, 45), null, now, WARSAW)

        assertEquals(emptySet<NumericEntryProblem>(), before)
        assertEquals(setOf(MOMENT_AFTER_NOW), after)
    }

    @Test
    fun `a value at 0,0005 rounds up to the smallest value 0,001 and is accepted`() {
        val problems = problemsOfNew(draft("0.0005", TODAY, 9, 0))

        assertEquals(emptySet<NumericEntryProblem>(), problems)
    }

    @Test
    fun `BHV-ENTRY-R5 an edited value is checked also when the moment is not changed`() {
        val problems = numericEntryProblems(draft("0", nov(10), 8, 30), stored, now, WARSAW)

        assertEquals(setOf(VALUE_NOT_ABOVE_ZERO), problems)
    }

    @Test
    fun `BHV-ENTRY-R5 an edit that moves the date to after today is after now`() {
        val problems = numericEntryProblems(draft("2", nov(12), 8, 30), stored, now, WARSAW)

        assertEquals(setOf(MOMENT_AFTER_NOW), problems)
    }

    @Test
    fun `both broken rules are reported together`() {
        val problems = problemsOfNew(draft("0", TODAY, 14, 0))

        assertEquals(setOf(VALUE_NOT_ABOVE_ZERO, MOMENT_AFTER_NOW), problems)
    }

    @Test
    fun `the current minute is not after now`() {
        val problems = problemsOfNew(draft("1", TODAY, 11, 30))

        assertEquals(emptySet<NumericEntryProblem>(), problems)
    }

    @Test
    fun `the next minute is after now`() {
        val problems = problemsOfNew(draft("1", TODAY, 11, 31))

        assertEquals(setOf(MOMENT_AFTER_NOW), problems)
    }

    private fun problemsOfNew(draft: NumericEntryDraft) = numericEntryProblems(draft, null, now, WARSAW)
}
