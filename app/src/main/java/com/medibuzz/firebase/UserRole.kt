package com.medibuzz.firebase

/**
 * Account role selected during registration.
 */
enum class UserRole {
    MEDICINE_USER,
    CARE_PARTNER;

    companion object {
        fun safeValueOf(value: String?): UserRole {
            return try {
                if (value.isNullOrBlank()) MEDICINE_USER else valueOf(value)
            } catch (e: IllegalArgumentException) {
                MEDICINE_USER
            }
        }
    }
}
