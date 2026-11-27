package kubisp.habittracker.domain.undo

import kotlinx.coroutines.flow.StateFlow
import kubisp.habittracker.domain.operation.DomainLock
import kubisp.habittracker.domain.port.Entries

/** Undo the current offer or drop it (`docs/DESIGN.md` sections 3 and 5.7, Decision 94). */
class UndoOperations(
    private val lock: DomainLock,
    private val entries: Entries,
    private val currentUndoOffer: CurrentUndoOffer
) {
    /** The current offer, or null when there is none; the App shell shows the Undo snackbar exactly while it holds one. */
    val offer: StateFlow<UndoOffer?> get() = currentUndoOffer.offer

    /** Brings back what the offer holds; does nothing when there is no offer. */
    suspend fun undo() {
        lock.withLock {
            when (val offer = currentUndoOffer.offer.value) {
                is UndoOffer.DeletedEntry -> entries.add(offer.entry)
                null -> return@withLock
            }
            currentUndoOffer.withdraw()
        }
    }

    /** The user dismissed the snackbar. */
    suspend fun dropOffer() {
        lock.withLock { currentUndoOffer.withdraw() }
    }
}
