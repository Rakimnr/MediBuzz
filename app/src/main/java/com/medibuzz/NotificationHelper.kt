package com.medibuzz

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * Builds and shows medicine reminder notifications with action buttons.
 */
object NotificationHelper {

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to take your medicine"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(
        context: Context,
        medicineId: Long,
        medicineName: String,
        scheduledTime: Long,
        logId: Long
    ) {
        createNotificationChannel(context)

        val notificationId = medicineId.toInt()

        // Tap notification opens the app
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Taken
        val takenIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = Constants.ACTION_TAKEN
            putExtra(Constants.EXTRA_MEDICINE_ID, medicineId)
            putExtra(Constants.EXTRA_MEDICINE_NAME, medicineName)
            putExtra(Constants.EXTRA_SCHEDULED_TIME, scheduledTime)
            putExtra(Constants.EXTRA_LOG_ID, logId)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = Constants.ACTION_SNOOZE
            putExtra(Constants.EXTRA_MEDICINE_ID, medicineId)
            putExtra(Constants.EXTRA_MEDICINE_NAME, medicineName)
            putExtra(Constants.EXTRA_SCHEDULED_TIME, scheduledTime)
            putExtra(Constants.EXTRA_LOG_ID, logId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Skipped
        val skippedIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = Constants.ACTION_SKIPPED
            putExtra(Constants.EXTRA_MEDICINE_ID, medicineId)
            putExtra(Constants.EXTRA_MEDICINE_NAME, medicineName)
            putExtra(Constants.EXTRA_SCHEDULED_TIME, scheduledTime)
            putExtra(Constants.EXTRA_LOG_ID, logId)
        }
        val skippedPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 3,
            skippedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val publicNotification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Medicine Reminder")
            .setContentText("It's time to take your medication")
            .build()

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title, medicineName))
            .setContentText(context.getString(R.string.notification_body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotification)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .addAction(0, context.getString(R.string.action_taken), takenPendingIntent)
            .addAction(0, context.getString(R.string.action_snooze), snoozePendingIntent)
            .addAction(0, context.getString(R.string.action_skipped), skippedPendingIntent)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, notification)
    }

    fun cancelNotification(context: Context, medicineId: Long) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.cancel(medicineId.toInt())
    }
}
