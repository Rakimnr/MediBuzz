package com.medibuzz.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MedicineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicine: Medicine): Long

    @Update
    suspend fun update(medicine: Medicine)

    @Query("SELECT * FROM medicines WHERE isActive = 1 ORDER BY hour, minute")
    suspend fun getAllActive(): List<Medicine>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getById(id: Long): Medicine?

    @Query("SELECT * FROM medicines ORDER BY createdAt DESC")
    suspend fun getAll(): List<Medicine>
}
