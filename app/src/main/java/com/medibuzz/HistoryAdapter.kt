package com.medibuzz

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
            val (statusText, statusColor, statusBg) = when (log.status) {
                ReminderStatus.TAKEN -> Triple(
                    context.getString(R.string.status_taken),
                    R.color.status_taken,
                    R.color.status_taken_bg
                )
                ReminderStatus.SNOOZED -> Triple(
                    context.getString(R.string.status_snoozed),
                    R.color.status_snoozed,
                    R.color.status_snoozed_bg
                )
                ReminderStatus.SKIPPED -> Triple(
                    context.getString(R.string.status_skipped),
                    R.color.status_skipped,
                    R.color.status_skipped_bg
                )
                ReminderStatus.MISSED -> Triple(
                    context.getString(R.string.status_missed),
                    R.color.status_missed,
                    R.color.status_missed_bg
                )
                ReminderStatus.PENDING -> Triple(
                    context.getString(R.string.status_pending),
                    R.color.status_pending,
                    R.color.status_pending_bg
                )
            }

            binding.tvStatus.text = statusText
            binding.tvStatus.setTextColor(ContextCompat.getColor(context, statusColor))
            binding.tvStatus.setBackgroundColor(ContextCompat.getColor(context, statusBg))

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
