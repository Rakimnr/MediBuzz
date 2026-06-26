package com.medibuzz

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medibuzz.data.Medicine
import com.medibuzz.data.MedicineRepository
import com.medibuzz.data.ScheduleType
import com.medibuzz.databinding.ActivityAddMedicineBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Screen for adding a new medicine reminder.
 */
class AddMedicineActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMedicineBinding
    private lateinit var repository: MedicineRepository

    private var selectedHour = 8
    private var selectedMinute = 0
    private var selectedStartDate = Calendar.getInstance()

    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = MedicineRepository(this)

        setupScheduleDropdown()
        updateTimeDisplay()
        updateDateDisplay()

        binding.btnPickTime.setOnClickListener { showTimePicker() }
        binding.btnPickDate.setOnClickListener { showDatePicker() }
        binding.btnSave.setOnClickListener { saveMedicine() }
    }

    private fun setupScheduleDropdown() {
        val options = listOf(
            getString(R.string.schedule_daily),
            getString(R.string.schedule_every_other_day)
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, options)
        binding.actvScheduleType.setAdapter(adapter)
        binding.actvScheduleType.setText(options[0], false)
        binding.actvScheduleType.keyListener = null
    }

    private fun showTimePicker() {
        TimePickerDialog(
            this,
            { _, hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                updateTimeDisplay()
            },
            selectedHour,
            selectedMinute,
            false
        ).show()
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedStartDate = Calendar.getInstance().apply {
                    set(year, month, day, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                updateDateDisplay()
            },
            selectedStartDate.get(Calendar.YEAR),
            selectedStartDate.get(Calendar.MONTH),
            selectedStartDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateTimeDisplay() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
        }
        binding.tvSelectedTime.text = timeFormat.format(cal.time)
    }

    private fun updateDateDisplay() {
        binding.tvSelectedDate.text = dateFormat.format(selectedStartDate.time)
    }

    private fun saveMedicine() {
        val name = binding.etMedicineName.text.toString().trim()
        val doseNote = binding.etDoseNote.text.toString().trim()

        if (name.isEmpty()) {
            binding.etMedicineName.error = getString(R.string.error_name_required)
            return
        }

        val scheduleText = binding.actvScheduleType.text.toString()
        val scheduleType = when (scheduleText) {
            getString(R.string.schedule_every_other_day) -> ScheduleType.EVERY_OTHER_DAY
            else -> ScheduleType.DAILY
        }

        val medicine = Medicine(
            name = name,
            doseNote = doseNote.ifEmpty { getString(R.string.no_dose_note) },
            hour = selectedHour,
            minute = selectedMinute,
            scheduleType = scheduleType,
            startDate = selectedStartDate.timeInMillis
        )

        lifecycleScope.launch {
            repository.insertMedicine(medicine)
            com.medibuzz.firebase.FirestoreSyncRepository(this@AddMedicineActivity).syncTodayScheduleIfEnabled()
            Toast.makeText(this@AddMedicineActivity, R.string.medicine_saved, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
