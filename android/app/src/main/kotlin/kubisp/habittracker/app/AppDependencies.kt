package kubisp.habittracker.app

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kubisp.habittracker.domain.entry.EntryOperations
import kubisp.habittracker.domain.habit.HabitOperations
import kubisp.habittracker.domain.operation.DomainLock
import kubisp.habittracker.domain.port.Habits
import kubisp.habittracker.domain.undo.CurrentUndoOffer
import kubisp.habittracker.domain.undo.UndoOperations
import kubisp.habittracker.persistence.HabitTrackerDatabase
import kubisp.habittracker.persistence.entry.RoomEntries
import kubisp.habittracker.persistence.habit.RoomHabits
import kubisp.habittracker.persistence.operation.RoomDataOperations
import kubisp.habittracker.platform.clock.DeviceClock
import kubisp.habittracker.platform.clock.systemTimeChanges

/**
 * The composition root (`docs/DESIGN.md` section 3, Decision 36): the only place that creates the concrete classes
 * and connects the port implementations to Domain. One instance lives as long as the process. All Domain operations
 * share one Domain lock and one undo offer (Decision 94).
 */
class AppDependencies(applicationContext: Context) {
    // The scope of the process: the expiry of the undo offer waits here, also while no screen is shown.
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val database = HabitTrackerDatabase.open(applicationContext)
    val habits: Habits = RoomHabits(database.habitDao())
    private val entries = RoomEntries(database)
    private val dataOperations = RoomDataOperations(database)

    // Each collector of the day or time zone changes registers its own receiver; only the screens on show collect it.
    private val clock = DeviceClock(systemTimeChanges(applicationContext))

    private val lock = DomainLock()
    private val currentUndoOffer = CurrentUndoOffer(lock, clock, applicationScope)

    val habitOperations = HabitOperations(lock, dataOperations, clock)
    val entryOperations = EntryOperations(lock, habits, entries, clock, currentUndoOffer)
    val undoOperations = UndoOperations(lock, entries, currentUndoOffer)
}
