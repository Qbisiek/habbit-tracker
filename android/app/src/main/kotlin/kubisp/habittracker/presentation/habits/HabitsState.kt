package kubisp.habittracker.presentation.habits

import java.util.UUID
import kubisp.habittracker.presentation.unit.UnitLabel

sealed interface HabitsState {
    /** Before the first read of the habits, so the empty state does not flash. */
    data object Loading : HabitsState

    /** UI-HABITS-LIST in display order (BHV-HABIT-R4); an empty list shows UI-HABITS-EMPTY. */
    data class Shown(val habits: List<HabitListRow>) : HabitsState
}

data class HabitListRow(val id: UUID, val name: String, val unit: UnitLabel)
