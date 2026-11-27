package kubisp.habittracker.gui.habitform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kubisp.habittracker.gui.PresenterFactories

/** Scoped to the destination of the form, so the typed values survive a rotation and a new form starts empty. */
class HabitFormViewModel(factories: PresenterFactories) : ViewModel() {
    val presenter = factories.habitFormPresenter(viewModelScope)
}
