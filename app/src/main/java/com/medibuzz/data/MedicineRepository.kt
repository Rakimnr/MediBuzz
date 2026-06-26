package com.medibuzz.data

import android.content.Context
import com.medibuzz.AlarmHelper
import com.medibuzz.ReminderSyncHelper


class MedicineRepository(private val context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val medicineDao = database.medicineDao()
    private val reminderLogDao = database.reminderLogDao()

    suspend fun insertMedicine(medicine: Medicine): Long {
        val id = medicineDao.insert(medicine)
        val saved = medicine.copy(id = id)
        AlarmHelper.scheduleNextAlarm(context, saved)
        return id
    }

    suspend fun getAllActiveMedicines(): List<Medicine> = medicineDao.getAllActive()

    suspend fun getMedicineById(id: Long): Medicine? = medicineDao.getById(id)

    suspend fun getAllReminderLogs(): List<ReminderLog> = reminderLogDao.getAll()

    suspend fun insertReminderLog(log: ReminderLog): Long {
        val id = reminderLogDao.insert(log)
        val saved = log.copy(id = id)
        ReminderSyncHelper.syncLog(context, saved)
        return id
    }

    suspend fun updateReminderLog(log: ReminderLog) {
        reminderLogDao.update(log)
        ReminderSyncHelper.syncLog(context, log)
    }

    suspend fun getReminderLogById(id: Long): ReminderLog? = reminderLogDao.getById(id)

    suspend fun getReminderLogByMedicineAndTime(
        medicineId: Long,
        scheduledTime: Long
    ): ReminderLog? = reminderLogDao.getByMedicineAndScheduledTime(medicineId, scheduledTime)

    suspend fun getReminderLogForMedicineOnDay(
        medicineId: Long,
        startOfDay: Long,
        endOfDay: Long
    ): ReminderLog? = reminderLogDao.getForMedicineOnDay(medicineId, startOfDay, endOfDay)
}