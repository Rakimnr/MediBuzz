package com.medibuzz.data

import androidx.room.TypeConverter

/**
 * Converts enums to strings for Room storage.
 */
class Converters {

    @TypeConverter
    fun fromScheduleType(value: ScheduleType): String = value.name

    @TypeConverter
    fun toScheduleType(value: String): ScheduleType = ScheduleType.valueOf(value)

    @TypeConverter
    fun fromReminderStatus(value: ReminderStatus): String = value.name

    @TypeConverter
    fun toReminderStatus(value: String): ReminderStatus = ReminderStatus.valueOf(value)
}
