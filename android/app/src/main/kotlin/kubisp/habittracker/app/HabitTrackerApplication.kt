package kubisp.habittracker.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kubisp.habittracker.gui.PresenterFactories
import kubisp.habittracker.presentation.shell.AppShellPresenter

/** Gives the objects that Android creates what their components need (Decision 71). */
class HabitTrackerApplication :
    Application(),
    PresenterFactories {
    private val dependencies by lazy { AppDependencies(this) }

    override fun appShellPresenter(scope: CoroutineScope): AppShellPresenter =
        AppShellPresenter(dependencies.undoOperations, scope)
}
