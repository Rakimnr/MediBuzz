package com.medibuzz

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.medibuzz.databinding.ActivitySettingsBinding
import com.medibuzz.firebase.FirebaseAuthRepository
import com.medibuzz.firebase.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.medibuzz.data.AppDatabase

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var authRepository: FirebaseAuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = FirebaseAuthRepository(this)

        binding.btnBack.setOnClickListener { finish() }

        binding.switchDarkMode.isChecked = MediBuzzPrefs.isDarkMode(this)
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            MediBuzzPrefs.setDarkMode(this, isChecked)
        }

        binding.btnSharingSettings.setOnClickListener {
            startActivity(Intent(this, SharingSettingsActivity::class.java))
        }

        binding.btnLogout.setOnClickListener { logout() }

        updatePermissionStatus()
        updateSharingButtonVisibility()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun updatePermissionStatus() {
        // Notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            binding.tvNotificationStatus.text = if (granted) {
                getString(R.string.permission_granted)
            } else {
                getString(R.string.permission_not_granted)
            }
        } else {
            binding.tvNotificationStatus.text = getString(R.string.permission_not_required)
        }

        // Exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            val canSchedule = alarmManager.canScheduleExactAlarms()
            binding.tvAlarmStatus.text = if (canSchedule) {
                getString(R.string.permission_granted)
            } else {
                getString(R.string.permission_not_granted)
            }
            binding.btnRequestAlarm.setOnClickListener {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        } else {
            binding.tvAlarmStatus.text = getString(R.string.permission_not_required)
            binding.btnRequestAlarm.visibility = View.GONE
        }
    }

    private fun updateSharingButtonVisibility() {
        lifecycleScope.launch {
            val profile = authRepository.getUserProfile()
            binding.btnSharingSettings.visibility = if (profile?.role == UserRole.MEDICINE_USER) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun logout() {
        authRepository.logout()
        Toast.makeText(this, R.string.logged_out, Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        // Run this before finishAffinity()
        lifecycleScope.launch(Dispatchers.IO) {
            AppDatabase.getDatabase(this@SettingsActivity).clearAllTables()
            // Optional: Call your AlarmHelper to cancel all active intents here
        }
        finishAffinity()
    }
}
