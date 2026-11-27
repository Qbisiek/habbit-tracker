package kubisp.habittracker.domain.undo

import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kubisp.habittracker.domain.entry.Entry
import kubisp.habittracker.domain.operation.DomainLock
import kubisp.habittracker.domain.port.Clock

/**
 * The one undo offer of Domain, kept in memory until it expires or is withdrawn (`docs/DESIGN.md` section 5.7,
 * Decision 93). App creates one instance and passes it to every class of Domain operations that creates, withdraws
 * or takes an offer; they call it under the Domain lock. [scope] is the application scope in which the expiry waits.
 */
class CurrentUndoOffer(private val lock: DomainLock, private val clock: Clock, private val scope: CoroutineScope) {
    private val current = MutableStateFlow<UndoOffer?>(null)
    private var expiry: Job? = null

    // Counts the offers made, so the expiry of a replaced offer can never drop its successor.
    private var offersMade = 0L

    internal val offer: StateFlow<UndoOffer?> = current.asStateFlow()

    /** A new offer replaces the previous one and expires [OFFER_DURATION] after now. */
    internal fun replaceWith(offer: UndoOffer) {
        expiry?.cancel()
        offersMade++
        val thisOffer = offersMade
        val expiresAt = clock.now().plus(OFFER_DURATION)
        current.value = offer
        expiry = scope.launch { expire(thisOffer, expiresAt) }
    }

    /** BHV-ENTRY-R6a: a new entry of the habit on a day that the deleted entry covers withdraws its offer. */
    internal fun withdrawIfConflictingWith(added: Entry) {
        val isWithdrawn = when (val offer = current.value) {
            is UndoOffer.DeletedEntry -> offer.isWithdrawnBy(added)
            null -> false
        }
        if (isWithdrawn) withdraw()
    }

    internal fun withdraw() {
        expiry?.cancel()
        expiry = null
        current.value = null
    }

    // Domain is the only clock of the offer: an Undo never compares the expiry with the device clock (Decision 93).
    private suspend fun expire(thisOffer: Long, expiresAt: Instant) {
        clock.waitUntil(expiresAt)
        lock.withLock {
            if (offersMade != thisOffer) return@withLock
            expiry = null
            current.value = null
        }
    }

    internal companion object {
        /** "A few seconds" of BHV-ENTRY-R6a and Decision 86. */
        val OFFER_DURATION: Duration = Duration.ofSeconds(5)
    }
}
