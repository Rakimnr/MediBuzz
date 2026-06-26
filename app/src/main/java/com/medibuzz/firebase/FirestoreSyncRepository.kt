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
            .await()
    }

    /**
     * Sync all pending local logs when app opens (catch up after offline).
     */
    suspend fun syncAllPendingLogs(logs: List<ReminderLog>) {
        logs.forEach { syncReminderLogIfEnabled(it) }
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
