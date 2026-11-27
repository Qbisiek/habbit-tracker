package kubisp.habittracker.presentation.day

import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kubisp.habittracker.domain.entry.Entry
import kubisp.habittracker.domain.entry.EntryOperations
import kubisp.habittracker.domain.entry.canAddEntriesOn
import kubisp.habittracker.domain.entry.numericValueOfDay
import kubisp.habittracker.domain.habit.Habit
import kubisp.habittracker.domain.habit.HabitId
import kubisp.habittracker.domain.habit.Measurement
import kubisp.habittracker.domain.habit.habitsOnDayScreen
import kubisp.habittracker.domain.port.Clock
import kubisp.habittracker.domain.port.Entries
import kubisp.habittracker.domain.port.Habits
import kubisp.habittracker.presentation.entryform.NumericEntryFormPresenter
import kubisp.habittracker.presentation.unit.toUnitLabel

/**
 * The Day screen (`docs/UX.md` section 3): the selected date and a row per habit with the value of that day. The
 * selected date follows today until the user picks a date (BHV-DAY-R1, `docs/DESIGN.md` section 5.4). The rows open
 * [numericEntryForm]. A change of the language tag while the screen lives comes with T4.7.
 * [scope] is the scope of its holder.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DayPresenter(
    private val habits: Habits,
    private val entries: Entries,
    private val entryOperations: EntryOperations,
    private val clock: Clock,
    val numericEntryForm: NumericEntryFormPresenter,
    private val languageTag: String,
    private val scope: CoroutineScope
) {
    private val selection = MutableStateFlow<DateSelection>(DateSelection.FollowsToday)

    // While nothing collects, a change of day is not signalled (Clock port), so the upstream stops with its
    // collectors and computes today again when the screen is shown again.
    val state: StateFlow<DayState> = combine(habits.habits(), calendarDays(), selection) { all, day, selection ->
        DayQuery(all, selection.dateOn(day.today), day)
    }
        .flatMapLatest { query -> stateOf(query) }
        .stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), DayState.Loading)

    fun onAction(action: DayAction) {
        when (action) {
            DayAction.ShowPreviousDay -> select(selectedDate().minusDays(1))
            DayAction.ShowNextDay -> select(selectedDate().plusDays(1))
            is DayAction.PickDate -> select(action.date)
            is DayAction.AddPlusStep -> addPlusStep(HabitId(action.habitId))
            is DayAction.TypeValue -> openNumericEntryForm(action.habitId)
        }
    }

    // BHV-DAY-R1: choosing today's date, by the picker or an arrow, puts the screen back to following today.
    private fun select(date: LocalDate) {
        selection.value = if (date == today()) DateSelection.FollowsToday else DateSelection.Picked(date)
    }

    private fun addPlusStep(habitId: HabitId) {
        val date = selectedDate()
        scope.launch {
            // The user may leave the screen right after the tap; the entry is written all the same.
            withContext(NonCancellable) { entryOperations.addPlusStep(habitId, date) }
        }
    }

    private fun openNumericEntryForm(habitId: UUID) {
        val shown = state.value as? DayState.Shown ?: return
        val rows = shown.habits as? DayHabits.Rows ?: return
        val row = rows.rows.find { it.habitId == habitId } ?: return
        numericEntryForm.openNew(HabitId(habitId), row.name, row.unit, shown.date)
    }

    private fun selectedDate(): LocalDate = selection.value.dateOn(today())

    private fun today(): LocalDate = clock.now().atZone(clock.timeZone()).toLocalDate()

    private fun calendarDays(): Flow<CalendarDay> = clock.dayOrTimeZoneChanges()
        .onStart { emit(Unit) }
        .map { CalendarDay(today(), clock.timeZone()) }
        .distinctUntilChanged()

    private fun stateOf(query: DayQuery): Flow<DayState> {
        val shownHabits = habitsOnDayScreen(query.habits, languageTag)
        if (shownHabits.isEmpty()) return flowOf(DayState.Shown(query.date, emptyHabitsOf(query.habits)))
        val rows = shownHabits.map { habit -> rowOf(habit, query) }
        return combine(rows) { DayState.Shown(query.date, DayHabits.Rows(it.toList())) }
    }

    private fun rowOf(habit: Habit, query: DayQuery): Flow<DayRow> = entries
        .entriesOverlapping(habit.id, query.date..query.date, query.day.timeZone)
        .map { habitEntries -> habit.toRow(habitEntries, query) }

    private companion object {
        // Long enough to keep the state over a rotation, so only leaving the screen stops the upstream.
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

private sealed interface DateSelection {
    data object FollowsToday : DateSelection

    data class Picked(val date: LocalDate) : DateSelection

    fun dateOn(today: LocalDate): LocalDate = when (this) {
        FollowsToday -> today
        is Picked -> date
    }
}

private data class CalendarDay(val today: LocalDate, val timeZone: ZoneId)

private data class DayQuery(val habits: List<Habit>, val date: LocalDate, val day: CalendarDay)

private fun emptyHabitsOf(habits: List<Habit>): DayHabits =
    if (habits.isEmpty()) DayHabits.NoHabits else DayHabits.AllArchived

private fun Habit.toRow(entries: List<Entry>, query: DayQuery): DayRow = DayRow(
    habitId = id.value,
    name = name,
    value = numericValueOfDay(entries, query.date).toBigDecimal(),
    unit = when (val measurement = measurement) {
        is Measurement.Numeric -> measurement.unit.toUnitLabel()
    },
    canAddEntries = canAddEntriesOn(query.date, query.day.today)
)
