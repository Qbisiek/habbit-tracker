package kubisp.habittracker.domain.operation

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * The one lock of Domain: write operations run one at a time (`docs/DESIGN.md` section 3, Decision 94). App creates
 * one instance and passes it to every class of Domain operations.
 */
class DomainLock {
    private val mutex = Mutex()

    /**
     * The lock is not reentrant (`docs/DESIGN.md` section 3): an operation that needs another one as a step calls
     * that step directly, not the public operation. Taking the lock again fails instead of waiting forever.
     */
    internal suspend fun <T> withLock(operation: suspend () -> T): T {
        check(currentCoroutineContext()[HeldLock]?.lock !== this) { "The Domain lock is already held" }
        return mutex.withLock { withContext(HeldLock(this)) { operation() } }
    }

    private class HeldLock(val lock: DomainLock) : CoroutineContext.Element {
        override val key: CoroutineContext.Key<*> get() = HeldLock

        companion object : CoroutineContext.Key<HeldLock>
    }
}
