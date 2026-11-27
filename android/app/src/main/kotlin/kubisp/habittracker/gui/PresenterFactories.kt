package kubisp.habittracker.gui

import kotlinx.coroutines.CoroutineScope
import kubisp.habittracker.presentation.habitform.HabitFormPresenter
import kubisp.habittracker.presentation.habits.HabitsPresenter
import kubisp.habittracker.presentation.shell.AppShellPresenter

/**
 * What the GUI needs from App (Decision 71): Android creates the Activity, so it reaches these factories by casting
 * the application context to this interface. Each factory gets the scope of the holder that keeps the object alive.
 */
interface PresenterFactories {
    fun appShellPresenter(scope: CoroutineScope): AppShellPresenter

    /** [languageTag]: the effective app language, read by the GUI from the current resource configuration. */
    fun habitsPresenter(scope: CoroutineScope, languageTag: String): HabitsPresenter

    fun habitFormPresenter(scope: CoroutineScope): HabitFormPresenter
}
