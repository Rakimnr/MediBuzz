package com.medibuzz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.recyclerview.widget.RecyclerView
import com.medibuzz.databinding.ItemPartnerStatusCardBinding
import com.medibuzz.firebase.SharedStatus
import java.text.SimpleDateFormat

class PartnerStatusAdapter(
    private var items: List<SharedStatus>,
    private val timeFormat: SimpleDateFormat
) : RecyclerView.Adapter<PartnerStatusAdapter.ViewHolder>() {

    fun updateItems(newItems: List<SharedStatus>) {
        this.items = newItems
        notifyItemInserted(items.size - 1)
    }

    inner class ViewHolder(private val binding: ItemPartnerStatusCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SharedStatus, position: Int) {
            binding.tvMedicineName.text = item.medicineName
            binding.tvScheduledTime.text = binding.root.context.getString(
                R.string.history_scheduled,
                timeFormat.format(item.scheduledTime)
            )
            StatusBadgeHelper.apply(binding.tvStatus, item.status)

            if (item.confirmedTime != null) {
                binding.tvConfirmedTime.text = binding.root.context.getString(
                    R.string.history_confirmed,
                    timeFormat.format(item.confirmedTime)
                )
                binding.tvConfirmedTime.visibility = View.VISIBLE
            } else {
                binding.tvConfirmedTime.visibility = View.GONE
            }

            // Fade-in animation
            val anim = AlphaAnimation(0f, 1f)
            anim.duration = 300
            anim.startOffset = position * 80L
            binding.root.startAnimation(anim)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPartnerStatusCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount() = items.size
}
