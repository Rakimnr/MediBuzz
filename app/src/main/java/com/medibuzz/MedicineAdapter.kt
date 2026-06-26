package com.medibuzz

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.medibuzz.data.ReminderStatus
import com.medibuzz.data.ScheduleType
import com.medibuzz.databinding.ItemMedicineCardBinding

/**
 * RecyclerView adapter for today's medicine cards on the home screen.
 */
class MedicineAdapter(
    private val items: List<TodayMedicineItem>
) : RecyclerView.Adapter<MedicineAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemMedicineCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TodayMedicineItem) {
            binding.tvMedicineName.text = item.name
            binding.tvDoseNote.text = item.doseNote
            binding.tvTime.text = item.timeText

            binding.tvScheduleType.text = when (item.scheduleType) {
                ScheduleType.DAILY -> binding.root.context.getString(R.string.schedule_daily)
                ScheduleType.EVERY_OTHER_DAY ->
                    binding.root.context.getString(R.string.schedule_every_other_day)
            }

            val context = binding.root.context
            val (statusText, statusColor, statusBg) = when (item.status) {
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
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicineCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
