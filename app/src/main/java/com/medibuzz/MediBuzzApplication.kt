package com.medibuzz

import android.app.Application

class MediBuzzApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MediBuzzPrefs.applyTheme(this)
    }
}
