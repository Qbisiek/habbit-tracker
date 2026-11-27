package kubisp.habittracker.domain.entry

/** A broken rule of a numeric entry (BHV-ENTRY-R5). */
enum class NumericEntryProblem {
    /** 0, a negative number, an empty field or not a number (BHV-ENTRY-9a, 9b). */
    VALUE_NOT_ABOVE_ZERO,

    /** Above 0 but 0.000 after rounding to 3 decimal places (BHV-ENTRY-9c, "The smallest value is 0.001"). */
    VALUE_BELOW_SMALLEST,

    /** The moment of an added entry, or a changed moment of an edited one, is after now. */
    MOMENT_AFTER_NOW
}
