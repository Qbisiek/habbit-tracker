package kubisp.habittracker.presentation.day

import java.time.LocalDate
import java.util.UUID

sealed interface DayAction {
    /** The previous arrow of UI-DAY-HEADER. */
    data object ShowPreviousDay : DayAction

    /** The next arrow of UI-DAY-HEADER. */
    data object ShowNextDay : DayAction

    /** A date chosen in UI-DAY-PICKER. */
    data class PickDate(val date: LocalDate) : DayAction

    /** The "+" button of a numeric habit (UI-DAY-ACTION). */
    data class AddPlusStep(val habitId: UUID) : DayAction

    /** The control to type a value (UI-DAY-ACTION): opens UI-ENTRYFORM-NUMERIC. */
    data class TypeValue(val habitId: UUID) : DayAction
}
