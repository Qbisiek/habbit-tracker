package kubisp.habittracker.spike

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** T1.5: the Stop of one habit removes only that habit's notification. */
class SpikeHabitStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habit = intent.getStringExtra(SpikeHabitNotifications.EXTRA_HABIT) ?: return
        SpikeHabitNotifications(context).remove(habit)
    }
}
