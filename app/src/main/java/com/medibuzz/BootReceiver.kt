package com.medibuzz

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.medibuzz.data.MedicineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reschedules all medicine alarms after device reboot.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = MedicineRepository(context)
                val medicines = repository.getAllActiveMedicines()
                medicines.forEach { medicine ->
                    AlarmHelper.scheduleNextAlarm(context, medicine)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
