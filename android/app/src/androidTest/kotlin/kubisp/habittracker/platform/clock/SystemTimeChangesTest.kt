package kubisp.habittracker.platform.clock

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.junit.runner.RunWith

// The system sends its minute tick only to receivers registered at run time, and an app cannot send it itself, so
// this test waits in real time for the next tick (up to a minute).
@RunWith(AndroidJUnit4::class)
class SystemTimeChangesTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun `the minute tick of the system reaches the stream while it is collected`() {
        runBlocking {
            withTimeout(MINUTE_TICK_TIMEOUT) {
                systemTimeChanges(context.applicationContext).first()
            }
        }
    }

    private companion object {
        val MINUTE_TICK_TIMEOUT = 65.seconds
    }
}
