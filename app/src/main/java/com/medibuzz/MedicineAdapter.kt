package com.medibuzz

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.recyclerview.widget.RecyclerView
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

        fun bind(item: TodayMedicineItem, position: Int) {
            binding.tvMedicineName.text = item.name
            binding.tvDoseNote.text = item.doseNote
            binding.tvTime.text = item.timeText

            binding.tvScheduleType.text = when (item.scheduleType) {
                ScheduleType.DAILY -> binding.root.context.getString(R.string.schedule_daily)
                ScheduleType.EVERY_OTHER_DAY ->
                    binding.root.context.getString(R.string.schedule_every_other_day)
            }

            StatusBadgeHelper.apply(binding.tvStatus, item.status)

            // Card fade-in animation
            val anim = AlphaAnimation(0f, 1f)
            anim.duration = 300
            anim.startOffset = position * 60L
            binding.root.startAnimation(anim)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicineCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount() = items.size
}
