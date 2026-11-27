package kubisp.habittracker.gui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kubisp.habittracker.gui.PresenterFactories

/** Scoped to the destination of the Habits list. */
class HabitsViewModel(factories: PresenterFactories, languageTag: String) : ViewModel() {
    val presenter = factories.habitsPresenter(viewModelScope, languageTag)
}
