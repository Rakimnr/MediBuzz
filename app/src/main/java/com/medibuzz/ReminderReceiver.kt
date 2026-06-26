package com.medibuzz

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.medibuzz.data.MedicineRepository
import com.medibuzz.data.ReminderLog
import com.medibuzz.data.ReminderStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receives alarm broadcasts and shows reminder notifications.
 * Also handles repeat reminders when user hasn't responded.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicineId = intent.getLongExtra(Constants.EXTRA_MEDICINE_ID, -1)
        val medicineName = intent.getStringExtra(Constants.EXTRA_MEDICINE_NAME) ?: return
        val scheduledTime = intent.getLongExtra(Constants.EXTRA_SCHEDULED_TIME, -1)
        val logId = intent.getLongExtra(Constants.EXTRA_LOG_ID, -1)

        if (medicineId == -1L || scheduledTime == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = MedicineRepository(context)
                val action = intent.action

                if (action == Constants.ACTION_REPEAT_REMINDER) {
                    // Repeat reminder — only show if still PENDING
                    val log = repository.getReminderLogById(logId)
                    if (log != null && log.status == ReminderStatus.PENDING) {
                        vibrate(context)
                        NotificationHelper.showReminderNotification(
                            context, medicineId, medicineName, scheduledTime, log.id
                        )
                        AlarmHelper.scheduleRepeatReminder(
                            context, medicineId, medicineName, scheduledTime, log.id
                        )
                    }
                } else {
                    // Main or snooze reminder alarm fired
                    val isSnooze = intent.getBooleanExtra("is_snooze", false)
                    var log = repository.getReminderLogByMedicineAndTime(medicineId, scheduledTime)
                    if (log == null) {
                        val newLog = ReminderLog(
                            medicineId = medicineId,
                            medicineName = medicineName,
                            scheduledTime = scheduledTime,
                            status = ReminderStatus.PENDING
                        )
                        val newId = repository.insertReminderLog(newLog)
                        log = newLog.copy(id = newId)
                    } else if (isSnooze) {
                        // After snooze, reset to PENDING so user can respond again
                        log = log.copy(status = ReminderStatus.PENDING, confirmedTime = null)
                        repository.updateReminderLog(log)
                    }

                    // Remind if PENDING (or was reset from snooze)
                    if (log.status == ReminderStatus.PENDING) {
                        vibrate(context)
                        NotificationHelper.showReminderNotification(
                            context, medicineId, medicineName, scheduledTime, log.id
                        )
                        AlarmHelper.scheduleRepeatReminder(
                            context, medicineId, medicineName, scheduledTime, log.id
                        )
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

  /**
     * Vibrate the phone when a reminder appears.
     */
    private fun vibrate(context: Context) {
        val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }
}
