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
        fun safeValueOf(value: String?): ReminderStatus {
            return try {
                if (value.isNullOrBlank()) PENDING else valueOf(value)
            } catch (e: IllegalArgumentException) {
                PENDING
            }
        }
    }
}
