package com.medibuzz.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Manages partner connections and sharing settings in Firestore.
 * Partner link document ID = medicineUserId (one partner per medicine user in MVP).
 */
class PartnerRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun findUserByPartnerCode(code: String): UserProfile? {
        val snapshot = firestore.collection(FirestoreCollections.USERS)
            .whereEqualTo("partnerCode", code.uppercase().trim())
            .limit(1)
            .get()
            .await()
        if (snapshot.isEmpty) return null
        val doc = snapshot.documents.first()
        return UserProfile.fromMap(doc.data ?: emptyMap())
    }

    /**
     * Care partner connects to medicine user using partner code.
     */
    suspend fun connectPartner(partnerCode: String): Result<PartnerLink> {
        val carePartnerId = auth.currentUser?.uid
            ?: return Result.failure(Exception("Not logged in"))

        val medicineUser = findUserByPartnerCode(partnerCode)
            ?: return Result.failure(Exception("Partner code not found"))

        if (medicineUser.role != UserRole.MEDICINE_USER) {
            return Result.failure(Exception("This code belongs to a care partner, not a medicine user"))
        }

        if (medicineUser.uid == carePartnerId) {
            return Result.failure(Exception("You cannot connect to yourself"))
        }

        // Document ID = medicineUserId for simple security rules
        val docId = medicineUser.uid
        val existing = getPartnerLinkForMedicineUser(docId)
        if (existing != null) {
            return Result.success(existing)
        }

        val link = PartnerLink(
            id = docId,
            medicineUserId = medicineUser.uid,
            carePartnerId = carePartnerId,
            sharingEnabled = false,
            createdAt = System.currentTimeMillis()
        )

        firestore.collection(FirestoreCollections.PARTNER_LINKS)
            .document(docId)
            .set(link.toMap())
            .await()

        return Result.success(link)
    }

    suspend fun getPartnerLinkForMedicineUser(medicineUserId: String): PartnerLink? {
        val doc = firestore.collection(FirestoreCollections.PARTNER_LINKS)
            .document(medicineUserId)
            .get()
            .await()
        if (!doc.exists()) return null
        return PartnerLink.fromMap(doc.id, doc.data ?: emptyMap())
    }

    suspend fun getPartnerLinkForCarePartner(carePartnerId: String): PartnerLink? {
        val snapshot = firestore.collection(FirestoreCollections.PARTNER_LINKS)
            .whereEqualTo("carePartnerId", carePartnerId)
            .limit(1)
            .get()
            .await()
        if (snapshot.isEmpty) return null
        val doc = snapshot.documents.first()
        return PartnerLink.fromMap(doc.id, doc.data ?: emptyMap())
    }

    suspend fun setSharingEnabled(linkId: String, enabled: Boolean): Result<Unit> {
        return try {
            firestore.collection(FirestoreCollections.PARTNER_LINKS)
                .document(linkId)
                .update("sharingEnabled", enabled)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removePartnerLink(linkId: String): Result<Unit> {
        return try {
            firestore.collection(FirestoreCollections.PARTNER_LINKS)
                .document(linkId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
