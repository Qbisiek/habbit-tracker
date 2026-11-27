package kubisp.habittracker.domain.entry

/**
 * The entries of a day as they are listed: by time (BHV-ENTRY-R8), entries with equal times in the order they were
 * added (BHV-ENTRY-R6b), which is the order of [entries] as read from the Entries port. `sortedBy` is stable.
 */
fun numericEntriesByTime(entries: List<Entry.Numeric>): List<Entry.Numeric> = entries.sortedBy { it.dateTime }
