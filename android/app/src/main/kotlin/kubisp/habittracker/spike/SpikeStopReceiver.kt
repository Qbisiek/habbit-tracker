package kubisp.habittracker.spike

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** T1.4: the Stop of the spike always "fails", so the notification is posted again with the not-stopped text. */
class SpikeStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val startedAtMillis = intent.getLongExtra(SpikeTimerNotification.EXTRA_STARTED_AT_MILLIS, 0L)
        SpikeTimerNotification(context).showNotStopped(startedAtMillis)
    }
}
