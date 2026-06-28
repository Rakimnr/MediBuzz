package com.medibuzz.data

/**
 * Status of a single reminder occurrence.
 */
enum class ReminderStatus {
    PENDING,
    TAKEN,
    SNOOZED,
    SKIPPED,
    MISSED;

    companion object {
        @Suppress("unused")
        fun safeValueOf(value: String?): ReminderStatus {
            return try {
                if (value.isNullOrBlank()) PENDING else valueOf(value)
            } catch (e: IllegalArgumentException) {
                android.util.Log.e("ReminderStatus", "Failed to parse enum", e)
                PENDING
            }
        }
    }
}
