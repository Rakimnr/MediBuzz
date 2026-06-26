package com.medibuzz

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.medibuzz.data.MedicineRepository
import com.medibuzz.data.ReminderStatus
import com.medibuzz.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Home screen showing today's medicine reminders.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: MedicineRepository
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled silently; reminders still work with vibration */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = MedicineRepository(this)
        NotificationHelper.createNotificationChannel(this)
        requestNotificationPermissionIfNeeded()

        binding.tvTodayDate.text = dateFormat.format(Calendar.getInstance().time)

        binding.btnAddMedicine.setOnClickListener {
            startActivity(Intent(this, AddMedicineActivity::class.java))
        }

        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        loadTodayMedicines()
    }

    override fun onResume() {
        super.onResume()
        loadTodayMedicines()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
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
                .filter { AlarmHelper.isMedicineScheduledForDay(it, todayStart) }
                .map { medicine ->
                    val scheduledTime = AlarmHelper.getScheduledTimeForDay(medicine, todayStart)
                    val log = repository.getReminderLogForMedicineOnDay(
                        medicine.id, todayStart.timeInMillis, todayEnd.timeInMillis
                    )

                    val status = log?.status ?: determineStatus(medicine, scheduledTime)

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
                    medicines.find { it.id == item.medicineId }?.let {
                        it.hour * 60 + it.minute
                    } ?: 0
                }

            if (todayItems.isEmpty()) {
                binding.tvEmptyState.visibility = android.view.View.VISIBLE
                binding.rvMedicines.visibility = android.view.View.GONE
            } else {
                binding.tvEmptyState.visibility = android.view.View.GONE
                binding.rvMedicines.visibility = android.view.View.VISIBLE
                binding.rvMedicines.layoutManager = LinearLayoutManager(this@MainActivity)
                binding.rvMedicines.adapter = MedicineAdapter(todayItems)
            }
        }
    }

    /**
     * If no log exists yet, infer status from whether the scheduled time has passed.
     */
    private fun determineStatus(medicine: com.medibuzz.data.Medicine, scheduledTime: Long): ReminderStatus {
        val now = System.currentTimeMillis()
        if (scheduledTime > now) {
            return ReminderStatus.PENDING
        }
        // Past scheduled time with no response — show as pending until end of day
        return ReminderStatus.PENDING
    }
}
