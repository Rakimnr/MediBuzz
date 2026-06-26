package com.medibuzz

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.medibuzz.data.MedicineRepository
import com.medibuzz.data.ReminderLog
import com.medibuzz.data.ReminderStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles notification action button presses: Taken, Snooze, Skipped.
 */
class NotificationActionReceiver : BroadcastReceiver() {

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
                val now = System.currentTimeMillis()

                when (intent.action) {
                    Constants.ACTION_TAKEN -> {
                        updateLog(repository, logId, medicineId, scheduledTime, medicineName,
                            ReminderStatus.TAKEN, now)
                        NotificationHelper.cancelNotification(context, medicineId)
                        AlarmHelper.cancelRepeatReminder(context, medicineId)

                        // Schedule next regular dose
                        val medicine = repository.getMedicineById(medicineId)
                        if (medicine != null) {
                            val nextTime = AlarmHelper.calculateNextReminderAfterAction(
                                medicine, scheduledTime
                            )
                            AlarmHelper.scheduleAlarmAt(context, medicine, nextTime)
                        }
                    }

                    Constants.ACTION_SNOOZE -> {
                        updateLog(repository, logId, medicineId, scheduledTime, medicineName,
                            ReminderStatus.SNOOZED, now)
                        NotificationHelper.cancelNotification(context, medicineId)
                        AlarmHelper.cancelRepeatReminder(context, medicineId)

                        // Schedule snooze alarm in 10 minutes
                        AlarmHelper.scheduleSnoozeAlarm(
                            context, medicineId, medicineName, scheduledTime
                        )
                    }

                    Constants.ACTION_SKIPPED -> {
                        updateLog(repository, logId, medicineId, scheduledTime, medicineName,
                            ReminderStatus.SKIPPED, now)
                        NotificationHelper.cancelNotification(context, medicineId)
                        AlarmHelper.cancelRepeatReminder(context, medicineId)

                        // Schedule next regular dose
                        val medicine = repository.getMedicineById(medicineId)
                        if (medicine != null) {
                            val nextTime = AlarmHelper.calculateNextReminderAfterAction(
                                medicine, scheduledTime
                            )
                            AlarmHelper.scheduleAlarmAt(context, medicine, nextTime)
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun updateLog(
        repository: MedicineRepository,
        logId: Long,
        medicineId: Long,
        scheduledTime: Long,
        medicineName: String,
        status: ReminderStatus,
        confirmedTime: Long
    ) {
        val existing = if (logId != -1L) {
            repository.getReminderLogById(logId)
        } else {
            repository.getReminderLogByMedicineAndTime(medicineId, scheduledTime)
        }

        if (existing != null) {
            repository.updateReminderLog(
                existing.copy(status = status, confirmedTime = confirmedTime)
            )
        } else {
            repository.insertReminderLog(
                ReminderLog(
                    medicineId = medicineId,
                    medicineName = medicineName,
                    scheduledTime = scheduledTime,
                    status = status,
                    confirmedTime = confirmedTime
                )
            )
        }
    }
}
