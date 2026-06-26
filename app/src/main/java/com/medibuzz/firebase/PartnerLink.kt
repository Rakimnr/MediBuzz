package com.medibuzz.firebase

/**
 * Link between a medicine user and their care partner.
 * Stored in the partner_links Firestore collection.
 */
data class PartnerLink(
    val id: String = "",
    val medicineUserId: String = "",
    val carePartnerId: String = "",
    val sharingEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "medicineUserId" to medicineUserId,
        "carePartnerId" to carePartnerId,
        "sharingEnabled" to sharingEnabled,
        "createdAt" to createdAt
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): PartnerLink {
            return PartnerLink(
                id = id,
                medicineUserId = map["medicineUserId"] as? String ?: "",
                carePartnerId = map["carePartnerId"] as? String ?: "",
                sharingEnabled = map["sharingEnabled"] as? Boolean ?: false,
                createdAt = map["createdAt"] as? Long ?: 0L
            )
        }
    }
}
