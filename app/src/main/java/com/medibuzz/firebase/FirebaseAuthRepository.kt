package com.medibuzz.firebase

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Handles Firebase Authentication and user profile storage.
 */
class FirebaseAuthRepository(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = currentUser != null

    suspend fun login(email: String, password: String): Result<UserProfile> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            val profile = getUserProfile()
            if (profile != null) {
                Result.success(profile)
            } else {
                Result.failure(Exception("User profile not found. Please register again."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        role: UserRole
    ): Result<UserProfile> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Registration failed")

            val partnerCode = if (role == UserRole.MEDICINE_USER) {
                generateUniquePartnerCode(uid)
            } else {
                null
            }

            val profile = UserProfile(
                uid = uid,
                email = email,
                role = role,
                displayName = displayName,
                partnerCode = partnerCode,
                createdAt = System.currentTimeMillis()
            )

            firestore.collection(FirestoreCollections.USERS)
                .document(uid)
                .set(profile.toMap())
                .await()

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): UserProfile? {
        val uid = currentUser?.uid ?: return null
        return try {
            val snapshot = firestore.collection(FirestoreCollections.USERS)
                .document(uid)
                .get()
                .await()
            if (snapshot.exists()) {
                UserProfile.fromMap(snapshot.data ?: emptyMap())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserProfileById(uid: String): UserProfile? {
        return try {
            val snapshot = firestore.collection(FirestoreCollections.USERS)
                .document(uid)
                .get()
                .await()
            if (snapshot.exists()) {
                UserProfile.fromMap(snapshot.data ?: emptyMap())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun ensurePartnerCode(): String? {
        val user = currentUser ?: return null
        val profile = getUserProfile() ?: return null
        if (profile.role != UserRole.MEDICINE_USER) return null
        if (!profile.partnerCode.isNullOrBlank()) return profile.partnerCode
        
        val newCode = generateUniquePartnerCode(user.uid)
        firestore.collection(FirestoreCollections.USERS).document(user.uid)
            .update("partnerCode", newCode).await()
        return newCode
    }

    private suspend fun generateUniquePartnerCode(uid: String): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val secureRandom = java.security.SecureRandom()
        repeat(10) {
            val code = (1..8).map { chars[secureRandom.nextInt(chars.length)] }.joinToString("")
            val docRef = firestore.collection(FirestoreCollections.PARTNER_CODES).document(code)
            val existing = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (!snapshot.exists()) {
                    transaction.set(docRef, mapOf("medicineUserId" to uid, "createdAt" to System.currentTimeMillis()))
                    false // not existing
                } else {
                    true // existing
                }
            }.await()
            if (!existing) {
                return code
            }
        }
        val fallbackCode = "MB" + uid.take(6).uppercase()
        val docRef = firestore.collection(FirestoreCollections.PARTNER_CODES).document(fallbackCode)
        docRef.set(mapOf("medicineUserId" to uid, "createdAt" to System.currentTimeMillis())).await()
        return fallbackCode
    }
}
