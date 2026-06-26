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
                generateUniquePartnerCode()
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

    private suspend fun generateUniquePartnerCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        repeat(10) {
            val code = (1..8).map { chars.random() }.joinToString("")
            val existing = firestore.collection(FirestoreCollections.USERS)
                .whereEqualTo("partnerCode", code)
                .get()
                .await()
            if (existing.isEmpty) return code
        }
        return currentUser?.uid?.take(8)?.uppercase() ?: "MEDIBUZZ"
    }
}
