package kubisp.habittracker.presentation.shell

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kubisp.habittracker.domain.undo.UndoOffer
import kubisp.habittracker.domain.undo.UndoOperations

/**
 * The app-level Presentation object (`docs/DESIGN.md` section 3, App shell; Decision 94). It alone follows the undo
 * offer of Domain, so its snackbar is shown once, whichever screens are open. [scope] is the scope of its holder.
 */
class AppShellPresenter(private val undoOperations: UndoOperations, private val scope: CoroutineScope) {
    private var shownOffer: UndoOffer? = null
    private var offersShown = 0L

    // The snackbar follows the offer stream exactly, with no timeout of its own (`docs/DESIGN.md` section 5.7).
    val state: StateFlow<AppShellState> = undoOperations.offer
        .map { offer -> AppShellState(snackbar = snackbarOf(offer)) }
        .stateIn(scope, SharingStarted.Eagerly, AppShellState(snackbar = snackbarOf(undoOperations.offer.value)))

    fun onAction(action: AppShellAction) {
        when (action) {
            AppShellAction.Undo -> scope.launch { undoOperations.undo() }
            AppShellAction.DismissSnackbar -> scope.launch { undoOperations.dropOffer() }
        }
    }

    // Numbers each new offer; the same offer seen again (the first value of the stream) keeps its number.
    private fun snackbarOf(offer: UndoOffer?): ShellSnackbar? {
        if (offer != shownOffer) {
            shownOffer = offer
            if (offer != null) offersShown++
        }
        return when (offer) {
            is UndoOffer.DeletedEntry -> ShellSnackbar.UndoDeletedEntry(offerNumber = offersShown)
            null -> null
        }
    }
}
