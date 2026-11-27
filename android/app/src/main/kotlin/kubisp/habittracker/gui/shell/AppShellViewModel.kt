package kubisp.habittracker.gui.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kubisp.habittracker.gui.PresenterFactories

/** Activity-scoped, so the App shell and the snackbar it shows survive a rotation and a recreation (Decision 94). */
class AppShellViewModel(factories: PresenterFactories) : ViewModel() {
    val presenter = factories.appShellPresenter(viewModelScope)
}
