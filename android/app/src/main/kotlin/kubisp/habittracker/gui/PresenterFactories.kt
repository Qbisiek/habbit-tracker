package kubisp.habittracker.gui

import kotlinx.coroutines.CoroutineScope
import kubisp.habittracker.presentation.shell.AppShellPresenter

/**
 * What the GUI needs from App (Decision 71): Android creates the Activity, so it reaches these factories by casting
 * the application context to this interface. Each factory gets the scope of the holder that keeps the object alive.
 */
interface PresenterFactories {
    fun appShellPresenter(scope: CoroutineScope): AppShellPresenter
}
