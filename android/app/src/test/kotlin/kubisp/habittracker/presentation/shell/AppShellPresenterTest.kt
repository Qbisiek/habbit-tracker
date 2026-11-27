package kubisp.habittracker.presentation.shell

import java.time.Duration
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kubisp.habittracker.domain.entry.Entry
import kubisp.habittracker.domain.entry.EntryOperations
import kubisp.habittracker.domain.fake.FakeClock
import kubisp.habittracker.domain.fake.FakeStorage
import kubisp.habittracker.domain.fake.aHabit
import kubisp.habittracker.domain.fake.aNumericEntry
import kubisp.habittracker.domain.operation.DomainLock
import kubisp.habittracker.domain.undo.CurrentUndoOffer
import kubisp.habittracker.domain.undo.UndoOperations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppShellPresenterTest {
    private val storage = FakeStorage()
    private val lock = DomainLock()
    private val clock = FakeClock()
    private val water = aHabit(name = "Water")
    private val today: LocalDate = LocalDate.of(2026, 11, 10)

    @Test
    fun `without an undo offer no snackbar is shown`() = runTest {
        val shell = givenShell()

        runCurrent()

        assertNull(shell.presenter.state.value.snackbar)
    }

    @Test
    fun `a deleted entry shows the undo snackbar`() = runTest {
        val shell = givenShell()
        val entry = storedEntry()

        shell.entries.deleteEntry(entry)
        runCurrent()

        assertTrue(shell.presenter.state.value.snackbar is ShellSnackbar.UndoDeletedEntry)
    }

    @Test
    fun `a second deletion replaces the undo snackbar and Undo brings back the second entry`() = runTest {
        val shell = givenShell()
        val first = storedEntry()
        val second = storedEntry()
        shell.entries.deleteEntry(first)
        runCurrent()
        val firstSnackbar = shell.presenter.state.value.snackbar

        shell.entries.deleteEntry(second)
        runCurrent()
        val secondSnackbar = shell.presenter.state.value.snackbar
        shell.presenter.onAction(AppShellAction.Undo)
        runCurrent()

        assertTrue(secondSnackbar is ShellSnackbar.UndoDeletedEntry)
        assertNotEquals(firstSnackbar, secondSnackbar)
        assertEquals(listOf(second), entriesOfToday())
        assertNull(shell.presenter.state.value.snackbar)
    }

    @Test
    fun `the undo snackbar disappears when the offer expires`() = runTest {
        val shell = givenShell()
        shell.entries.deleteEntry(storedEntry())
        runCurrent()

        clock.advanceBy(Duration.ofMinutes(1))
        runCurrent()

        assertNull(shell.presenter.state.value.snackbar)
    }

    @Test
    fun `the undo snackbar disappears when the offer is withdrawn by a new entry on the same day`() = runTest {
        val shell = givenShell()
        shell.entries.deleteEntry(storedEntry())
        runCurrent()

        shell.entries.addPlusStep(water.id, today)
        runCurrent()

        assertNull(shell.presenter.state.value.snackbar)
    }

    @Test
    fun `Undo brings the entry back and hides the snackbar`() = runTest {
        val shell = givenShell()
        val entry = storedEntry()
        shell.entries.deleteEntry(entry)
        runCurrent()

        shell.presenter.onAction(AppShellAction.Undo)
        runCurrent()

        assertEquals(listOf(entry), entriesOfToday())
        assertNull(shell.presenter.state.value.snackbar)
    }

    @Test
    fun `dismissing the snackbar drops the offer without bringing the entry back`() = runTest {
        val shell = givenShell()
        val entry = storedEntry()
        shell.entries.deleteEntry(entry)
        runCurrent()

        shell.presenter.onAction(AppShellAction.DismissSnackbar)
        runCurrent()

        assertEquals(emptyList<Entry>(), entriesOfToday())
        assertNull(shell.presenter.state.value.snackbar)
        assertNull(shell.undo.offer.value)
    }

    private suspend fun TestScope.givenShell(): Shell {
        storage.saveHabit(water)
        return Shell(backgroundScope)
    }

    private inner class Shell(scope: CoroutineScope) {
        private val currentUndoOffer = CurrentUndoOffer(lock, clock, scope)
        val entries = EntryOperations(lock, storage, storage, clock, currentUndoOffer)
        val undo = UndoOperations(lock, storage, currentUndoOffer)
        val presenter = AppShellPresenter(undo, scope)
    }

    private suspend fun storedEntry(): Entry.Numeric =
        aNumericEntry(water.id, dateTime = today.atTime(8, 0)).also { storage.add(it) }

    private suspend fun entriesOfToday(): List<Entry> =
        storage.entriesOverlapping(water.id, today..today, clock.timeZone()).first()
}
