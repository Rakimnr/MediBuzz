package com.medibuzz.firebase

/**
 * Firestore user profile stored in the users collection.
 */
data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val role: UserRole = UserRole.MEDICINE_USER,
    val displayName: String = "",
    val partnerCode: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "uid" to uid,
            "email" to email,
            "role" to role.name,
            "displayName" to displayName,
            "createdAt" to createdAt
        )
        if (partnerCode != null) {
            map["partnerCode"] = partnerCode
        }
        return map
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): UserProfile {
            return UserProfile(
                uid = map["uid"] as? String ?: "",
                email = map["email"] as? String ?: "",
                role = UserRole.valueOf(map["role"] as? String ?: UserRole.MEDICINE_USER.name),
                displayName = map["displayName"] as? String ?: "",
                partnerCode = map["partnerCode"] as? String,
                createdAt = map["createdAt"] as? Long ?: 0L
            )
        }
    }
}
