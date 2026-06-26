package com.medibuzz

import android.app.Application

/**
 * Application entry point — applies saved theme before activities launch.
 */
class MediBuzzApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MediBuzzPrefs.applyTheme(this)
    }
}
