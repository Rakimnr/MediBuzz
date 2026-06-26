package com.medibuzz

import com.medibuzz.data.ReminderStatus
import com.medibuzz.data.ScheduleType

/**
 * Display model for a medicine scheduled today on the home screen.
 */
data class TodayMedicineItem(
    val medicineId: Long,
    val name: String,
    val doseNote: String,
    val timeText: String,
    val scheduleType: ScheduleType,
    val status: ReminderStatus
)
