package com.medibuzz

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.medibuzz.data.MedicineRepository
import com.medibuzz.data.ReminderStatus
import com.medibuzz.databinding.ActivityMainBinding
import com.medibuzz.firebase.FirebaseAuthRepository
import com.medibuzz.firebase.FirestoreSyncRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Home screen showing today's medicine reminders with progress dashboard.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: MedicineRepository
    private lateinit var authRepository: FirebaseAuthRepository

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // Permission result handled silently.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = MedicineRepository(this)
        authRepository = FirebaseAuthRepository(this)

        NotificationHelper.createNotificationChannel(this)
        requestNotificationPermissionIfNeeded()

        binding.tvTodayDate.text = dateFormat.format(Calendar.getInstance().time)

        binding.btnAddMedicine.setOnClickListener {
            startActivity(Intent(this, AddMedicineActivity::class.java))
        }
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.btnPartner.setOnClickListener {
            startActivity(Intent(this, SharingSettingsActivity::class.java))
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        loadGreeting()
        loadTodayMedicines()
        syncPendingLogs()
    }

    override fun onResume() {
        super.onResume()
        loadTodayMedicines()
        syncPendingLogs()
        lifecycleScope.launch {
            FirestoreSyncRepository(this@MainActivity).syncTodayScheduleIfEnabled()
        }
    }

    private fun loadGreeting() {
        lifecycleScope.launch {
            val profile = authRepository.getUserProfile()
            val name = profile?.displayName?.ifEmpty { null }
                ?: getString(R.string.greeting_default)
            binding.tvGreeting.text = getString(R.string.greeting_user, name)
        }
    }

    /**
     * Sync existing local logs into shared_status.
     * This is required because the care partner dashboard reads shared_status,
     * not users/{uid}/reminderLogs.
     */
    private fun syncPendingLogs() {
        lifecycleScope.launch {
            try {
                val logs = repository.getAllReminderLogs()
                FirestoreSyncRepository(this@MainActivity).syncAllPendingLogs(logs)
            } catch (_: Exception) {
                // Do not block the home screen if Firebase is offline/unavailable.
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun loadTodayMedicines() {
        lifecycleScope.launch {
            val medicines = repository.getAllActiveMedicines()

            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val todayEnd = Calendar.getInstance().apply {
                timeInMillis = todayStart.timeInMillis
                add(Calendar.DAY_OF_YEAR, 1)
            }

            val todayItems = medicines
                .filter { medicine ->
                    AlarmHelper.isMedicineScheduledForDay(medicine, todayStart)
                }
                .map { medicine ->
                    val scheduledTime = AlarmHelper.getScheduledTimeForDay(medicine, todayStart)
                    val log = repository.getReminderLogForMedicineOnDay(
                        medicine.id,
                        todayStart.timeInMillis,
                        todayEnd.timeInMillis
                    )
                    val status = log?.status ?: ReminderStatus.PENDING

                    TodayMedicineItem(
                        medicineId = medicine.id,
                        name = medicine.name,
                        doseNote = medicine.doseNote,
                        timeText = timeFormat.format(scheduledTime),
                        scheduleType = medicine.scheduleType,
                        status = status
                    )
                }
                .sortedBy { item ->
                    medicines.find { it.id == item.medicineId }
                        ?.let { it.hour * 60 + it.minute } ?: 0
                }

            updateDashboard(todayItems)

            if (todayItems.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.rvMedicines.visibility = View.GONE
                binding.layoutProgress.visibility = View.GONE
                binding.layoutSummary.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.rvMedicines.visibility = View.VISIBLE
                binding.layoutProgress.visibility = View.VISIBLE
                binding.layoutSummary.visibility = View.VISIBLE

                binding.rvMedicines.layoutManager = LinearLayoutManager(this@MainActivity)
                binding.rvMedicines.adapter = MedicineAdapter(todayItems)

                val anim = AlphaAnimation(0f, 1f)
                anim.duration = 400
                binding.layoutProgress.startAnimation(anim)
            }
        }
    }

    private fun updateDashboard(items: List<TodayMedicineItem>) {
        val total = items.size
        val taken = items.count { it.status == ReminderStatus.TAKEN }
        val pending = items.count {
            it.status == ReminderStatus.PENDING || it.status == ReminderStatus.SNOOZED
        }
        val missed = items.count { it.status == ReminderStatus.MISSED }
        val skipped = items.count { it.status == ReminderStatus.SKIPPED }

        binding.tvSummaryTaken.text = taken.toString()
        binding.tvSummaryPending.text = pending.toString()
        binding.tvSummaryMissed.text = missed.toString()
        binding.tvSummarySkipped.text = skipped.toString()

        val percent = if (total > 0) (taken * 100) / total else 0
        binding.tvProgressPercent.text = getString(R.string.progress_percent, percent)
        binding.progressCircle.progress = percent
    }
}
