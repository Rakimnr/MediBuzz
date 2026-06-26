package com.medibuzz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A medicine the user wants to be reminded about.
 */
@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val doseNote: String,
    val hour: Int,
    val minute: Int,
    val scheduleType: ScheduleType,
    val startDate: Long,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
