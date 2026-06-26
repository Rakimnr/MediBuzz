package com.medibuzz

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.medibuzz.data.Medicine
import com.medibuzz.data.ScheduleType
import java.util.Calendar

/**
 * Handles scheduling and calculating medicine reminder alarms.
 */
object AlarmHelper {

    /**
     * Schedule the next reminder alarm for a medicine (first or upcoming dose).
     */
    fun scheduleNextAlarm(context: Context, medicine: Medicine) {
        if (!medicine.isActive) return

        val triggerTime = calculateNextReminderTime(medicine)
        scheduleAlarmAt(context, medicine, triggerTime)
    }

    /**
     * Schedule an alarm at a specific trigger time.
     */
    fun scheduleAlarmAt(context: Context, medicine: Medicine, triggerTime: Long) {
        if (!medicine.isActive) return
        if (triggerTime <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = Constants.ACTION_REMINDER
            putExtra(Constants.EXTRA_MEDICINE_ID, medicine.id)
            putExtra(Constants.EXTRA_MEDICINE_NAME, medicine.name)
            putExtra(Constants.EXTRA_SCHEDULED_TIME, triggerTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicine.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactAlarm(alarmManager, triggerTime, pendingIntent)
    }

    /**
     * Schedule a snooze alarm (10 minutes from now).
     */
    fun scheduleSnoozeAlarm(
        context: Context,
        medicineId: Long,
        medicineName: String,
        originalScheduledTime: Long
    ) {
        val triggerTime = System.currentTimeMillis() + Constants.SNOOZE_DURATION_MS
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = Constants.ACTION_REMINDER
            putExtra(Constants.EXTRA_MEDICINE_ID, medicineId)
            putExtra(Constants.EXTRA_MEDICINE_NAME, medicineName)
            putExtra(Constants.EXTRA_SCHEDULED_TIME, originalScheduledTime)
            putExtra("is_snooze", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (medicineId.toInt() + 10000),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactAlarm(alarmManager, triggerTime, pendingIntent)
    }

    /**
     * Schedule a repeat reminder when user has not responded (keeps buzzing).
     */
    fun scheduleRepeatReminder(
        context: Context,
        medicineId: Long,
        medicineName: String,
        scheduledTime: Long,
        logId: Long
    ) {
        val triggerTime = System.currentTimeMillis() + Constants.REPEAT_REMINDER_INTERVAL_MS
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = Constants.ACTION_REPEAT_REMINDER
            putExtra(Constants.EXTRA_MEDICINE_ID, medicineId)
            putExtra(Constants.EXTRA_MEDICINE_NAME, medicineName)
            putExtra(Constants.EXTRA_SCHEDULED_TIME, scheduledTime)
            putExtra(Constants.EXTRA_LOG_ID, logId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (medicineId.toInt() + 20000),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactAlarm(alarmManager, triggerTime, pendingIntent)
    }

    /**
     * Cancel repeat reminder alarms for a medicine.
     */
    fun cancelRepeatReminder(context: Context, medicineId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = Constants.ACTION_REPEAT_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (medicineId.toInt() + 20000),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Cancel the main reminder alarm for a medicine.
     */
    fun cancelAlarm(context: Context, medicineId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = Constants.ACTION_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicineId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        cancelRepeatReminder(context, medicineId)
    }

  /**
     * Calculate the next reminder time based on schedule type and start date.
     * Daily = next occurrence at same hour/minute.
     * Every other day = every 2 days from start date at same hour/minute.
     */
    fun calculateNextReminderTime(medicine: Medicine): Long {
        val now = Calendar.getInstance()
        val candidate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, medicine.hour)
            set(Calendar.MINUTE, medicine.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startCal = Calendar.getInstance().apply {
            timeInMillis = medicine.startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        when (medicine.scheduleType) {
            ScheduleType.DAILY -> {
                // If today's time hasn't passed and start date is today or earlier, use today
                if (candidate.timeInMillis > now.timeInMillis &&
                    !candidate.before(startCal)
                ) {
                    return candidate.timeInMillis
                }
                // Otherwise find next valid day
                candidate.add(Calendar.DAY_OF_YEAR, 1)
                while (candidate.before(startCal)) {
                    candidate.add(Calendar.DAY_OF_YEAR, 1)
                }
                return candidate.timeInMillis
            }

            ScheduleType.EVERY_OTHER_DAY -> {
                // Find next day that aligns with every-other-day schedule from start date
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                var checkDay = if (candidate.timeInMillis > now.timeInMillis &&
                    !todayStart.before(startCal) &&
                    isEveryOtherDayMatch(startCal, todayStart)
                ) {
                    todayStart
                } else {
                    Calendar.getInstance().apply {
                        timeInMillis = todayStart.timeInMillis
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }

                // Search up to 365 days ahead for a valid every-other-day slot
                for (i in 0 until 365) {
                    if (!checkDay.before(startCal) && isEveryOtherDayMatch(startCal, checkDay)) {
                        val result = Calendar.getInstance().apply {
                            timeInMillis = checkDay.timeInMillis
                            set(Calendar.HOUR_OF_DAY, medicine.hour)
                            set(Calendar.MINUTE, medicine.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        if (result.timeInMillis > now.timeInMillis) {
                            return result.timeInMillis
                        }
                    }
                    checkDay.add(Calendar.DAY_OF_YEAR, 1)
                }

                return candidate.timeInMillis
            }
        }
    }

    /**
     * Calculate next reminder after a dose was taken or skipped.
     */
    fun calculateNextReminderAfterAction(medicine: Medicine, lastScheduledTime: Long): Long {
        val base = Calendar.getInstance().apply {
            timeInMillis = lastScheduledTime
            set(Calendar.HOUR_OF_DAY, medicine.hour)
            set(Calendar.MINUTE, medicine.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        when (medicine.scheduleType) {
            ScheduleType.DAILY -> base.add(Calendar.DAY_OF_YEAR, 1)
            ScheduleType.EVERY_OTHER_DAY -> base.add(Calendar.DAY_OF_YEAR, 2)
        }

        return base.timeInMillis
    }

    /**
     * Check if a medicine is scheduled for a given calendar day.
     */
    fun isMedicineScheduledForDay(medicine: Medicine, dayStart: Calendar): Boolean {
        if (!medicine.isActive) return false

        val startCal = Calendar.getInstance().apply {
            timeInMillis = medicine.startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (dayStart.before(startCal)) return false

        return when (medicine.scheduleType) {
            ScheduleType.DAILY -> true
            ScheduleType.EVERY_OTHER_DAY -> isEveryOtherDayMatch(startCal, dayStart)
        }
    }

    /**
     * Get the scheduled time millis for a medicine on a specific day.
     */
    fun getScheduledTimeForDay(medicine: Medicine, dayStart: Calendar): Long {
        return Calendar.getInstance().apply {
            timeInMillis = dayStart.timeInMillis
            set(Calendar.HOUR_OF_DAY, medicine.hour)
            set(Calendar.MINUTE, medicine.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun isEveryOtherDayMatch(startCal: Calendar, dayCal: Calendar): Boolean {
        val startMs = startCal.timeInMillis
        val dayMs = dayCal.timeInMillis
        val daysDiff = (dayMs - startMs) / (24 * 60 * 60 * 1000L)
        return daysDiff % 2 == 0L
    }

    private fun scheduleExactAlarm(
        alarmManager: AlarmManager,
        triggerTime: Long,
        pendingIntent: PendingIntent
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                // Fallback if exact alarm permission not granted
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
}
