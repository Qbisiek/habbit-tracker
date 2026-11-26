package kubisp.habittracker.spike

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kubisp.habittracker.R

/**
 * T1.5: one ongoing notification per habit, identified by its tag, and the list of the app's shown notifications
 * that the notification sync (docs/DESIGN.md section 5.6) would read.
 */
class SpikeHabitNotifications(private val context: Context) {
    private val manager = NotificationManagerCompat.from(context)

    /** Returns false when the notification permission is not granted, so nothing was posted. */
    fun show(habit: String, startedAtMillis: Long): Boolean {
        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        if (permission != PackageManager.PERMISSION_GRANTED) return false
        createChannel()
        val notification =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(context.getString(R.string.spike_active_notification_title, habit))
                .setOngoing(true)
                .setWhen(startedAtMillis)
                .setUsesChronometer(true)
                .addAction(0, context.getString(R.string.spike_notification_stop), stopIntent(habit))
                .build()
        manager.notify(tagOf(habit), NOTIFICATION_ID, notification)
        return true
    }

    fun remove(habit: String) = manager.cancel(tagOf(habit), NOTIFICATION_ID)

    fun shown(): List<ShownNotification> = manager.activeNotifications.map {
        ShownNotification(tag = it.tag, id = it.id, isOngoing = it.isOngoing, postTimeMillis = it.postTime)
    }

    private fun createChannel() {
        val channel =
            NotificationChannelCompat
                .Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(context.getString(R.string.spike_channel_name))
                .build()
        manager.createNotificationChannel(channel)
    }

    // A request code per habit: with one shared code, FLAG_UPDATE_CURRENT would give every Stop the last habit's extra.
    private fun stopIntent(habit: String): PendingIntent {
        val intent = Intent(context, SpikeHabitStopReceiver::class.java).putExtra(EXTRA_HABIT, habit)
        return PendingIntent.getBroadcast(
            context,
            requestCodeOf(habit),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun tagOf(habit: String) = "$TAG_PREFIX$habit"

    private fun requestCodeOf(habit: String) = REQUEST_CODE_BASE + HABITS.indexOf(habit)

    companion object {
        val HABITS = listOf("A", "B", "C")
        const val EXTRA_HABIT = "habit"
        private const val TAG_PREFIX = "habit:"
        private const val CHANNEL_ID = "spike_timer"
        private const val NOTIFICATION_ID = 2
        private const val REQUEST_CODE_BASE = 100
    }
}

data class ShownNotification(val tag: String?, val id: Int, val isOngoing: Boolean, val postTimeMillis: Long)
