package com.medibuzz

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medibuzz.data.ReminderLog
import com.medibuzz.data.ReminderStatus
import com.medibuzz.databinding.ItemHistoryCardBinding
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * RecyclerView adapter for the history screen.
 */
class HistoryAdapter(
    private val logs: List<ReminderLog>
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val timeFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
    private val confirmedFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    inner class ViewHolder(private val binding: ItemHistoryCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(log: ReminderLog) {
            binding.tvMedicineName.text = log.medicineName
            binding.tvScheduledTime.text = binding.root.context.getString(
                R.string.history_scheduled,
                timeFormat.format(log.scheduledTime)
            )

            val context = binding.root.context
            StatusBadgeHelper.apply(binding.tvStatus, log.status)

            if (log.confirmedTime != null) {
                binding.tvConfirmedTime.text = context.getString(
                    R.string.history_confirmed,
                    confirmedFormat.format(log.confirmedTime)
                )
                binding.tvConfirmedTime.visibility = android.view.View.VISIBLE
            } else {
                binding.tvConfirmedTime.visibility = android.view.View.GONE
            }

            // Show missed dose safety message for skipped/missed statuses
            if (log.status == ReminderStatus.SKIPPED || log.status == ReminderStatus.MISSED) {
                binding.tvMissedAdvice.text = context.getString(R.string.missed_dose_advice)
                binding.tvMissedAdvice.visibility = android.view.View.VISIBLE
            } else {
                binding.tvMissedAdvice.visibility = android.view.View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(logs[position])
    }

    override fun getItemCount() = logs.size
}
