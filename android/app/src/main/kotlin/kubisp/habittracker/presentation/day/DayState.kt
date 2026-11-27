package kubisp.habittracker.presentation.day

import java.time.LocalDate

sealed interface DayState {
    /** Before the first read of the habits, so the empty state does not flash. */
    data object Loading : DayState

    /** [date]: the selected date of UI-DAY-HEADER (BHV-DAY-R1). */
    data class Shown(val date: LocalDate, val habits: DayHabits) : DayState
}
