package com.medibuzz.data

/**
 * Status of a single reminder occurrence.
 */
enum class ReminderStatus {
    PENDING,
    TAKEN,
    SNOOZED,
    SKIPPED,
    MISSED
}
