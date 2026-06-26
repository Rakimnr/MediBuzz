package com.medibuzz.firebase

import com.medibuzz.data.ReminderStatus

/**
 * Shared reminder status synced to Firestore for care partner visibility.
 */
data class SharedStatus(
    val id: String = "",
    val medicineUserId: String = "",
    val carePartnerId: String = "",
    val medicineId: Long = 0,
    val medicineName: String = "",
    val scheduledTime: Long = 0,
    val status: ReminderStatus = ReminderStatus.PENDING,
    val confirmedTime: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "medicineUserId" to medicineUserId,
            "carePartnerId" to carePartnerId,
            "medicineId" to medicineId,
            "medicineName" to medicineName,
            "scheduledTime" to scheduledTime,
            "status" to status.name,
            "updatedAt" to updatedAt
        )
        if (confirmedTime != null) {
            map["confirmedTime"] = confirmedTime
        }
        return map
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): SharedStatus {
            return SharedStatus(
                id = id,
                medicineUserId = map["medicineUserId"] as? String ?: "",
                carePartnerId = map["carePartnerId"] as? String ?: "",
                medicineId = (map["medicineId"] as? Long) ?: (map["medicineId"] as? Number)?.toLong() ?: 0L,
                medicineName = map["medicineName"] as? String ?: "",
                scheduledTime = (map["scheduledTime"] as? Long) ?: (map["scheduledTime"] as? Number)?.toLong() ?: 0L,
                status = ReminderStatus.valueOf(map["status"] as? String ?: ReminderStatus.PENDING.name),
                confirmedTime = (map["confirmedTime"] as? Long) ?: (map["confirmedTime"] as? Number)?.toLong(),
                updatedAt = (map["updatedAt"] as? Long) ?: (map["updatedAt"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}
