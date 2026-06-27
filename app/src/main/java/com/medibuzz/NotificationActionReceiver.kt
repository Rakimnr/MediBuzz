package com.medibuzz

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.medibuzz.data.MedicineRepository
import com.medibuzz.data.ReminderLog
import com.medibuzz.data.ReminderStatus
import com.medibuzz.firebase.FirestoreSyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                        val updatedLog = updateLog(
                            repository = repository,
                            logId = logId,
                            medicineId = medicineId,
                            scheduledTime = scheduledTime,
                            medicineName = medicineName,
                            status = ReminderStatus.TAKEN,
                            confirmedTime = now
                        )
                        syncSharedStatus(context, updatedLog)

                        NotificationHelper.cancelNotification(context, medicineId)
                        AlarmHelper.cancelRepeatReminder(context, medicineId)

                        val medicine = repository.getMedicineById(medicineId)
                        if (medicine != null) {
                            val nextTime = AlarmHelper.calculateNextReminderAfterAction(
                                medicine,
                                scheduledTime
                            )
                            AlarmHelper.scheduleAlarmAt(context, medicine, nextTime)
                        }
                    }

                    Constants.ACTION_SNOOZE -> {
                        val updatedLog = updateLog(
                            repository = repository,
                            logId = logId,
                            medicineId = medicineId,
                            scheduledTime = scheduledTime,
                            medicineName = medicineName,
                            status = ReminderStatus.SNOOZED,
                            confirmedTime = now
                        )
                        syncSharedStatus(context, updatedLog)

                        NotificationHelper.cancelNotification(context, medicineId)
                        AlarmHelper.cancelRepeatReminder(context, medicineId)

                        AlarmHelper.scheduleSnoozeAlarm(
                            context,
                            medicineId,
                            medicineName,
                            scheduledTime
                        )
                    }

                    Constants.ACTION_SKIPPED -> {
                        val updatedLog = updateLog(
                            repository = repository,
                            logId = logId,
                            medicineId = medicineId,
                            scheduledTime = scheduledTime,
                            medicineName = medicineName,
                            status = ReminderStatus.SKIPPED,
                            confirmedTime = now
                        )
                        syncSharedStatus(context, updatedLog)

                        NotificationHelper.cancelNotification(context, medicineId)
                        AlarmHelper.cancelRepeatReminder(context, medicineId)

                        val medicine = repository.getMedicineById(medicineId)
                        if (medicine != null) {
                            val nextTime = AlarmHelper.calculateNextReminderAfterAction(
                                medicine,
                                scheduledTime
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
    ): ReminderLog {
        val existing = if (logId != -1L) {
            repository.getReminderLogById(logId)
        } else {
            repository.getReminderLogByMedicineAndTime(medicineId, scheduledTime)
        }

        return if (existing != null) {
            val updated = existing.copy(
                status = status,
                confirmedTime = confirmedTime
            )
            repository.updateReminderLog(updated)
            updated
        } else {
            val newLog = ReminderLog(
                medicineId = medicineId,
                medicineName = medicineName,
                scheduledTime = scheduledTime,
                status = status,
                confirmedTime = confirmedTime
            )
            val newId = repository.insertReminderLog(newLog)
            newLog.copy(id = newId)
        }
    }

    private suspend fun syncSharedStatus(context: Context, log: ReminderLog) {
        try {
            FirestoreSyncRepository(context).syncReminderLogIfEnabled(log)
        } catch (_: Exception) {
            // Keep notification actions working even if the device is offline
            // or Firebase temporarily fails.
        }
    }
}
