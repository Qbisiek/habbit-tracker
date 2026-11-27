package kubisp.habittracker.domain.operation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DomainLockTest {
    private val lock = DomainLock()

    @Test
    fun `a write operation waits until the running one has finished`() = runTest {
        val steps = mutableListOf<String>()
        val firstMayFinish = CompletableDeferred<Unit>()
        launch {
            lock.withLock {
                steps += "first started"
                firstMayFinish.await()
                steps += "first finished"
            }
        }
        runCurrent()

        launch { lock.withLock { steps += "second started" } }
        runCurrent()
        firstMayFinish.complete(Unit)
        runCurrent()

        assertEquals(listOf("first started", "first finished", "second started"), steps)
    }

    @Test
    fun `a failed write operation releases the lock`() = runTest {
        runCatching { lock.withLock { error("write failed") } }

        val result = lock.withLock { "next operation" }

        assertEquals("next operation", result)
    }

    @Test
    fun `taking the lock inside a write operation fails instead of waiting forever`() = runTest {
        val takeTheLockTwice = suspend { lock.withLock { lock.withLock { } } }

        val failure = runCatching { takeTheLockTwice() }.exceptionOrNull()

        assertTrue("$failure", failure is IllegalStateException)
    }
}
