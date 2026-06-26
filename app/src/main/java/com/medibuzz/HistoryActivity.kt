package com.medibuzz

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.medibuzz.data.MedicineRepository
import com.medibuzz.databinding.ActivityHistoryBinding
import kotlinx.coroutines.launch

/**
 * Screen showing all reminder history logs.
 */
class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var repository: MedicineRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = MedicineRepository(this)

        binding.btnBack.setOnClickListener { finish() }

        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            val logs = repository.getAllReminderLogs()

            if (logs.isEmpty()) {
                binding.tvEmptyState.visibility = android.view.View.VISIBLE
                binding.rvHistory.visibility = android.view.View.GONE
            } else {
                binding.tvEmptyState.visibility = android.view.View.GONE
                binding.rvHistory.visibility = android.view.View.VISIBLE
                binding.rvHistory.layoutManager = LinearLayoutManager(this@HistoryActivity)
                binding.rvHistory.adapter = HistoryAdapter(logs)
            }
        }
    }
}
