package kubisp.habittracker.spike

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import kubisp.habittracker.R

/**
 * T1.4: posts an ongoing timer notification whose texts are resolved from the given context, so the spike shows
 * which language a context outside an Activity (the Stop receiver) resolves.
 */
class SpikeTimerNotification(private val context: Context) {
    private val manager = context.getSystemService(NotificationManager::class.java)

    fun showRunning(startedAtMillis: Long) = post(R.string.spike_notification_running, startedAtMillis)

    fun showNotStopped(startedAtMillis: Long) = post(R.string.spike_notification_not_stopped, startedAtMillis)

    private fun post(@StringRes text: Int, startedAtMillis: Long) {
        createChannel()
        val notification =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(context.getString(R.string.spike_notification_title))
                .setContentText(context.getString(text))
                .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(text)))
                .setSubText(resolvedLocales())
                .setOngoing(true)
                .setWhen(startedAtMillis)
                .setUsesChronometer(true)
                .addAction(0, context.getString(R.string.spike_notification_stop), stopIntent(startedAtMillis))
                .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    // Creating an existing channel again updates its name, which is how the spike checks the channel language.
    private fun createChannel() {
        val name = context.getString(R.string.spike_channel_name)
        manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW))
    }

    private fun resolvedLocales(): String = context.resources.configuration.locales
        .toLanguageTags()

    private fun stopIntent(startedAtMillis: Long): PendingIntent {
        val intent =
            Intent(context, SpikeStopReceiver::class.java).putExtra(EXTRA_STARTED_AT_MILLIS, startedAtMillis)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    companion object {
        const val EXTRA_STARTED_AT_MILLIS = "startedAtMillis"
        private const val CHANNEL_ID = "spike_timer"
        private const val NOTIFICATION_ID = 1
    }
}
