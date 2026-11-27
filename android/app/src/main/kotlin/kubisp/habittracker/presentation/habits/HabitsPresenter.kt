package kubisp.habittracker.presentation.habits

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kubisp.habittracker.domain.habit.Habit
import kubisp.habittracker.domain.habit.Measurement
import kubisp.habittracker.domain.habit.habitsOnHabitsScreen
import kubisp.habittracker.domain.port.Habits
import kubisp.habittracker.presentation.unit.toUnitLabel

/**
 * The Habits screen (`docs/UX.md` section 5): the habits of [habitsOnHabitsScreen] for [languageTag]. A change of the
 * language tag while the screen lives comes with T4.7.
 */
class HabitsPresenter(habits: Habits, languageTag: String, scope: CoroutineScope) {
    val state: StateFlow<HabitsState> = habits.habits()
        .map { all -> HabitsState.Shown(listRowsOf(all, languageTag)) }
        .stateIn(scope, SharingStarted.Eagerly, HabitsState.Loading)
}

private fun listRowsOf(habits: List<Habit>, languageTag: String): List<HabitListRow> =
    habitsOnHabitsScreen(habits, languageTag).map { it.toListRow() }

private fun Habit.toListRow(): HabitListRow = HabitListRow(
    id = id.value,
    name = name,
    unit = when (val measurement = measurement) {
        is Measurement.Numeric -> measurement.unit.toUnitLabel()
    }
)
