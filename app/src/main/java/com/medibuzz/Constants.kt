package com.medibuzz

object Constants {

    const val DATABASE_NAME = "medibuzz_database"

    // Notification channel
    const val NOTIFICATION_CHANNEL_ID = "medibuzz_reminder_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Medicine Reminders"

    // Alarm intents
    const val ACTION_REMINDER = "com.medibuzz.ACTION_REMINDER"
    const val ACTION_REPEAT_REMINDER = "com.medibuzz.ACTION_REPEAT_REMINDER"

    // Notification action intents
    const val ACTION_TAKEN = "com.medibuzz.ACTION_TAKEN"
    const val ACTION_SNOOZE = "com.medibuzz.ACTION_SNOOZE"
    const val ACTION_SKIPPED = "com.medibuzz.ACTION_SKIPPED"

    // Intent extras
    const val EXTRA_MEDICINE_ID = "extra_medicine_id"
    const val EXTRA_MEDICINE_NAME = "extra_medicine_name"
    const val EXTRA_SCHEDULED_TIME = "extra_scheduled_time"
    const val EXTRA_LOG_ID = "extra_log_id"

    // Repeat reminder interval when user has not responded (5 minutes)
    const val REPEAT_REMINDER_INTERVAL_MS = 5 * 60 * 1000L

    // Snooze duration (10 minutes)
    const val SNOOZE_DURATION_MS = 10 * 60 * 1000L
}
