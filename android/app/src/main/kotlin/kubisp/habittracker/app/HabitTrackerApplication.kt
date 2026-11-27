package kubisp.habittracker.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kubisp.habittracker.gui.PresenterFactories
import kubisp.habittracker.presentation.habitform.HabitFormPresenter
import kubisp.habittracker.presentation.habits.HabitsPresenter
import kubisp.habittracker.presentation.shell.AppShellPresenter

/** Gives the objects that Android creates what their components need (Decision 71). */
class HabitTrackerApplication :
    Application(),
    PresenterFactories {
    private val dependencies by lazy { AppDependencies(this) }

    override fun appShellPresenter(scope: CoroutineScope): AppShellPresenter =
        AppShellPresenter(dependencies.undoOperations, scope)

    override fun habitsPresenter(scope: CoroutineScope, languageTag: String): HabitsPresenter =
        HabitsPresenter(dependencies.habits, languageTag, scope)

    override fun habitFormPresenter(scope: CoroutineScope): HabitFormPresenter =
        HabitFormPresenter(dependencies.habitOperations, scope)
}
