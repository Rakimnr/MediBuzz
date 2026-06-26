package com.medibuzz

import android.content.Context
import android.content.Intent
import com.medibuzz.firebase.FirebaseAuthRepository
import com.medibuzz.firebase.UserRole

/**
 * Routes user to the correct home screen based on login state and role.
 */
object AuthNavigator {

    suspend fun navigateToHome(context: Context, finishCurrent: Boolean = true) {
        val authRepo = FirebaseAuthRepository(context)
        if (!authRepo.isLoggedIn) {
            context.startActivity(Intent(context, LoginActivity::class.java))
            if (finishCurrent && context is android.app.Activity) context.finish()
            return
        }

        val profile = authRepo.getUserProfile()
        val intent = when (profile?.role) {
            UserRole.CARE_PARTNER -> Intent(context, PartnerDashboardActivity::class.java)
            else -> Intent(context, MainActivity::class.java)
        }
        context.startActivity(intent)
        if (finishCurrent && context is android.app.Activity) context.finish()
    }
}
