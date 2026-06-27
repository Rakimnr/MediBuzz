package com.medibuzz.firebase

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.medibuzz.data.ReminderLog
import com.medibuzz.data.ReminderStatus
import kotlinx.coroutines.tasks.await
import java.util.Calendar

/**
 * Syncs local reminder logs to Firestore when sharing is enabled.
 */
class FirestoreSyncRepository(private val context: Context) {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val authRepository = FirebaseAuthRepository(context)
    private val partnerRepository = PartnerRepository()

    /**
     * Upload or update a reminder log to shared_status if sharing is enabled.
     */
    suspend fun syncReminderLogIfEnabled(log: ReminderLog) {
        val uid = auth.currentUser?.uid ?: return

        val profile = authRepository.getUserProfile() ?: return
        if (profile.role != UserRole.MEDICINE_USER) return

        val partnerLink = partnerRepository.getPartnerLinkForMedicineUser(uid) ?: return
        if (!partnerLink.sharingEnabled) return

        val docId = "${uid}_${log.medicineId}_${log.scheduledTime}"
        val sharedStatus = SharedStatus(
            id = docId,
            medicineUserId = uid,
            carePartnerId = partnerLink.carePartnerId,
            medicineId = log.medicineId,
            medicineName = log.medicineName,
            scheduledTime = log.scheduledTime,
            status = log.status,
            confirmedTime = log.confirmedTime,
            updatedAt = System.currentTimeMillis()
        )

        firestore.collection(FirestoreCollections.SHARED_STATUS)
            .document(docId)
            .set(sharedStatus.toMap())
            .addOnSuccessListener { /* Synced later */ }
            .addOnFailureListener { /* Handle error silently */ }
    }

    /**
     * Sync all pending local logs when app opens (catch up after offline).
     */
    suspend fun syncAllPendingLogs(logs: List<ReminderLog>) {
        if (logs.isEmpty()) return
        val uid = auth.currentUser?.uid ?: return
        val profile = authRepository.getUserProfile() ?: return
        if (profile.role != UserRole.MEDICINE_USER) return
        val partnerLink = partnerRepository.getPartnerLinkForMedicineUser(uid) ?: return
        if (!partnerLink.sharingEnabled) return

        val batch = firestore.batch()
        val collection = firestore.collection(FirestoreCollections.SHARED_STATUS)
        
        logs.forEach { log ->
            val docId = "${uid}_${log.medicineId}_${log.scheduledTime}"
            val sharedStatus = SharedStatus(
                id = docId,
                medicineUserId = uid,
                carePartnerId = partnerLink.carePartnerId,
                medicineId = log.medicineId,
                medicineName = log.medicineName,
                scheduledTime = log.scheduledTime,
                status = log.status,
                confirmedTime = log.confirmedTime,
                updatedAt = System.currentTimeMillis()
            )
            batch.set(collection.document(docId), sharedStatus.toMap())
        }
        
        try {
            batch.commit().await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreSyncRepo", "Failed to sync pending logs", e)
        }
    }

    /**
     * Sync today's active medicines as PENDING if they don't have a ReminderLog yet.
     */
    suspend fun syncTodayScheduleIfEnabled() {
        val uid = auth.currentUser?.uid ?: return
        val profile = authRepository.getUserProfile() ?: return
        if (profile.role != UserRole.MEDICINE_USER) return
        val partnerLink = partnerRepository.getPartnerLinkForMedicineUser(uid) ?: return
        if (!partnerLink.sharingEnabled) return

        val medicineRepository = com.medibuzz.data.MedicineRepository(context)
        val medicines = medicineRepository.getAllActiveMedicines()

        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayEnd = Calendar.getInstance().apply {
            timeInMillis = todayStart.timeInMillis
            add(Calendar.DAY_OF_YEAR, 1)
        }

        val batch = firestore.batch()
        var batchCount = 0
        val collection = firestore.collection(FirestoreCollections.SHARED_STATUS)

        medicines.filter { com.medibuzz.AlarmHelper.isMedicineScheduledForDay(it, todayStart) }
            .forEach { medicine ->
                val scheduledTime = com.medibuzz.AlarmHelper.getScheduledTimeForDay(medicine, todayStart)
                val log = medicineRepository.getReminderLogForMedicineOnDay(
                    medicine.id,
                    todayStart.timeInMillis,
                    todayEnd.timeInMillis
                )
                val status = log?.status ?: ReminderStatus.PENDING
                val confirmedTime = log?.confirmedTime

                val docId = "${uid}_${medicine.id}_$scheduledTime"
                val sharedStatus = SharedStatus(
                    id = docId,
                    medicineUserId = uid,
                    carePartnerId = partnerLink.carePartnerId,
                    medicineId = medicine.id,
                    medicineName = medicine.name,
                    scheduledTime = scheduledTime,
                    status = status,
                    confirmedTime = confirmedTime,
                    updatedAt = System.currentTimeMillis()
                )
                batch.set(collection.document(docId), sharedStatus.toMap())
                batchCount++
            }

        if (batchCount > 0) {
            try {
                batch.commit().await()
            } catch (e: Exception) {
                android.util.Log.e("FirestoreSyncRepo", "Failed to sync today schedule", e)
            }
        }
    }

    /**
     * Care partner: fetch today's shared statuses.
     */
    suspend fun getTodaySharedStatusesForPartner(carePartnerId: String): List<SharedStatus> {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayEnd = Calendar.getInstance().apply {
            timeInMillis = todayStart.timeInMillis
            add(Calendar.DAY_OF_YEAR, 1)
        }

        // Verify sharing is enabled for this partner
        val link = partnerRepository.getPartnerLinkForCarePartner(carePartnerId)
        if (link == null || !link.sharingEnabled) return emptyList()

        val snapshot = firestore.collection(FirestoreCollections.SHARED_STATUS)
            .whereEqualTo("carePartnerId", carePartnerId)
            .whereGreaterThanOrEqualTo("scheduledTime", todayStart.timeInMillis)
            .whereLessThan("scheduledTime", todayEnd.timeInMillis)
            .get()
            .await()

        return snapshot.documents.map { doc ->
            SharedStatus.fromMap(doc.id, doc.data ?: emptyMap())
        }
    }

    /**
     * Check if sharing is currently enabled for the logged-in medicine user.
     */
    suspend fun isSharingEnabled(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val link = partnerRepository.getPartnerLinkForMedicineUser(uid)
        return link?.sharingEnabled == true
    }
}
