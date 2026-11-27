package kubisp.habittracker.domain.entry

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kubisp.habittracker.domain.decimal.ExactDecimal
import kubisp.habittracker.domain.decimal.parseDecimalInput

/**
 * The rules of BHV-ENTRY-R5 that [draft] breaks. [stored] is the entry being edited, or null for a new entry; the
 * moment is checked against [now] only for a new entry or when its date, hour or minute differ from [stored]
 * (BHV-ENTRY-21a, 21d).
 */
fun numericEntryProblems(
    draft: NumericEntryDraft,
    stored: Entry.Numeric?,
    now: Instant,
    timeZone: ZoneId
): Set<NumericEntryProblem> = when (val checked = checkNumericDraft(draft, stored, now, timeZone)) {
    is CheckedNumericDraft.Valid -> emptySet()
    is CheckedNumericDraft.Invalid -> checked.problems
}

internal sealed interface CheckedNumericDraft {
    data class Valid(val value: ExactDecimal, val moment: LocalDateTime) : CheckedNumericDraft

    data class Invalid(val problems: Set<NumericEntryProblem>) : CheckedNumericDraft
}

internal fun checkNumericDraft(
    draft: NumericEntryDraft,
    stored: Entry.Numeric?,
    now: Instant,
    timeZone: ZoneId
): CheckedNumericDraft {
    val typed = parseDecimalInput(draft.value)
    val rounded = typed?.let { ExactDecimal.of(it) }
    val valueProblem = valueProblemOf(typed, rounded)
    val value = rounded?.takeIf { valueProblem == null }
    val moment = momentOfDraft(draft, stored)
    val isMomentAfterNow = isMomentChanged(draft, stored) && isAfterNow(moment, now, timeZone)
    if (value != null && !isMomentAfterNow) return CheckedNumericDraft.Valid(value, moment)
    val problems = buildSet {
        valueProblem?.let { add(it) }
        if (isMomentAfterNow) add(NumericEntryProblem.MOMENT_AFTER_NOW)
    }
    return CheckedNumericDraft.Invalid(problems)
}

// BHV-ENTRY-R5: the value must be above 0 after rounding to 3 decimal places (BHV-ENTRY-9c).
private fun valueProblemOf(typed: BigDecimal?, rounded: ExactDecimal?): NumericEntryProblem? = when {
    typed == null || rounded == null || typed.signum() <= 0 -> NumericEntryProblem.VALUE_NOT_ABOVE_ZERO
    rounded.toBigDecimal().signum() == 0 -> NumericEntryProblem.VALUE_BELOW_SMALLEST
    else -> null
}
