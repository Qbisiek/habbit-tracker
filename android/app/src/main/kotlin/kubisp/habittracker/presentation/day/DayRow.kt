package kubisp.habittracker.presentation.day

import java.math.BigDecimal
import java.util.UUID
import kubisp.habittracker.presentation.unit.UnitLabel

/**
 * [value]: the value of the selected day (UI-DAY-VALUE, BHV-ENTRY-R4). [canAddEntries]: the action controls are
 * shown (UI-DAY-ACTION); not on a date after today (BHV-ENTRY-R3).
 */
data class DayRow(
    val habitId: UUID,
    val name: String,
    val value: BigDecimal,
    val unit: UnitLabel,
    val canAddEntries: Boolean
)
