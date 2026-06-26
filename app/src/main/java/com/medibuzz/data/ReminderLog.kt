package com.medibuzz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Log entry for each reminder occurrence (scheduled dose).
 */
@Entity(tableName = "reminder_logs")
data class ReminderLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicineId: Long,
    val medicineName: String,
    val scheduledTime: Long,
    val status: ReminderStatus,
    val confirmedTime: Long? = null
)
