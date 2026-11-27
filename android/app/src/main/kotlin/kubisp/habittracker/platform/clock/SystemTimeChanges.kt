package kubisp.habittracker.platform.clock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

/**
 * Signals each system broadcast after which the calendar day or the time zone may have changed, while collected.
 * The minute tick catches midnight; the others a date change, a device clock set by the user and a new time zone.
 * Pass the application context, so the receiver does not hold an Activity.
 */
fun systemTimeChanges(context: Context): Flow<Unit> = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            trySend(Unit)
        }
    }
    // The docs ask for RECEIVER_EXPORTED for system broadcasts; these actions are protected, so only the system sends
    // them: https://developer.android.com/develop/background-work/background-tasks/broadcasts#context-registered-receivers
    context.registerReceiver(receiver, timeChangeBroadcasts(), Context.RECEIVER_EXPORTED)
    awaitClose { context.unregisterReceiver(receiver) }
}.conflate()

private fun timeChangeBroadcasts() = IntentFilter().apply {
    addAction(Intent.ACTION_TIME_TICK)
    addAction(Intent.ACTION_DATE_CHANGED)
    addAction(Intent.ACTION_TIME_CHANGED)
    addAction(Intent.ACTION_TIMEZONE_CHANGED)
}
