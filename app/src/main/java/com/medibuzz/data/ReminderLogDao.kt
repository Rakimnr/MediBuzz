package com.medibuzz.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReminderLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ReminderLog): Long

    @Update
    suspend fun update(log: ReminderLog)

    @Query("SELECT * FROM reminder_logs ORDER BY scheduledTime DESC")
    suspend fun getAll(): List<ReminderLog>

    @Query("SELECT * FROM reminder_logs WHERE id = :id")
    suspend fun getById(id: Long): ReminderLog?

    @Query(
        "SELECT * FROM reminder_logs WHERE medicineId = :medicineId " +
            "AND scheduledTime = :scheduledTime LIMIT 1"
    )
    suspend fun getByMedicineAndScheduledTime(medicineId: Long, scheduledTime: Long): ReminderLog?

    @Query(
        "SELECT * FROM reminder_logs WHERE medicineId = :medicineId " +
            "AND scheduledTime >= :startOfDay AND scheduledTime < :endOfDay LIMIT 1"
    )
    suspend fun getForMedicineOnDay(
        medicineId: Long,
        startOfDay: Long,
        endOfDay: Long
    ): ReminderLog?
}
