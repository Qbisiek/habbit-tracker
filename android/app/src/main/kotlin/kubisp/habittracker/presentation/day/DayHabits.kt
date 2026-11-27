package kubisp.habittracker.presentation.day

sealed interface DayHabits {
    /** UI-DAY-EMPTY-NONE: there is no habit at all. */
    data object NoHabits : DayHabits

    /** UI-DAY-EMPTY-ARCHIVED: every habit is archived. */
    data object AllArchived : DayHabits

    /** UI-DAY-ROW of each habit on the Day screen, in display order (BHV-HABIT-R4). */
    data class Rows(val rows: List<DayRow>) : DayHabits
}
