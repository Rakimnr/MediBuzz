package com.medibuzz

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.medibuzz.data.ReminderLog
import com.medibuzz.firebase.FirebaseAuthRepository
import kotlinx.coroutines.tasks.await

/**
 * Helper object for synchronizing reminder logs with Firebase Firestore.
 */
object ReminderSyncHelper {

    private const val TAG = "ReminderSyncHelper"

    /**
     * Syncs a single reminder log to Firestore.
     */
    fun syncLog(context: Context, log: ReminderLog) {
        val authRepository = FirebaseAuthRepository(context)
        val userId = authRepository.currentUser?.uid ?: return

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userId)
            .collection("reminderLogs")
            .document(log.id.toString())
            .set(log)
            .addOnFailureListener { e ->
                Log.e(TAG, "Error syncing log ${log.id}", e)
            }
    }

    /**
     * Syncs a list of reminder logs to Firestore using a batch write.
     */
    suspend fun syncAllLogs(context: Context, logs: List<ReminderLog>) {
        if (logs.isEmpty()) return

        val authRepository = FirebaseAuthRepository(context)
        val userId = authRepository.currentUser?.uid ?: return

        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()
        val logsCollection = db.collection("users")
            .document(userId)
            .collection("reminderLogs")

        logs.forEach { log ->
            val docRef = logsCollection.document(log.id.toString())
            batch.set(docRef, log)
        }

        try {
            batch.commit().await()
            Log.d(TAG, "Successfully synced ${logs.size} logs")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync all logs", e)
        }
    }
}
