package kubisp.habittracker.gui.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kubisp.habittracker.gui.PresenterFactories

/** Scoped to the destination of the Day screen, so the selected date and an open form survive a rotation. */
class DayViewModel(factories: PresenterFactories, languageTag: String) : ViewModel() {
    val presenter = factories.dayPresenter(viewModelScope, languageTag)
}
