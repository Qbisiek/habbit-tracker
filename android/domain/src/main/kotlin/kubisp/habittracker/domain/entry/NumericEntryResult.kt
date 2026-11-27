package kubisp.habittracker.domain.entry

sealed interface NumericEntryResult {
    data class Saved(val entry: Entry.Numeric) : NumericEntryResult

    /** Nothing was written. */
    data class Rejected(val problems: Set<NumericEntryProblem>) : NumericEntryResult
}
