package com.medibuzz

import android.widget.TextView
import androidx.core.content.ContextCompat
import com.medibuzz.data.ReminderStatus

/**
 * Applies consistent status badge colors across all screens.
 */
object StatusBadgeHelper {

    data class BadgeStyle(val textRes: Int, val textColor: Int, val bgColor: Int)

    fun getStyle(status: ReminderStatus): BadgeStyle {
        return when (status) {
            ReminderStatus.TAKEN -> BadgeStyle(
                R.string.status_taken, R.color.status_taken, R.color.status_taken_bg
            )
            ReminderStatus.PENDING -> BadgeStyle(
                R.string.status_pending, R.color.status_pending, R.color.status_pending_bg
            )
            ReminderStatus.SNOOZED -> BadgeStyle(
                R.string.status_snoozed, R.color.status_snoozed, R.color.status_snoozed_bg
            )
            ReminderStatus.SKIPPED -> BadgeStyle(
                R.string.status_skipped, R.color.status_skipped, R.color.status_skipped_bg
            )
            ReminderStatus.MISSED -> BadgeStyle(
                R.string.status_missed, R.color.status_missed, R.color.status_missed_bg
            )
        }
    }

    fun apply(textView: TextView, status: ReminderStatus) {
        val style = getStyle(status)
        val context = textView.context
        textView.text = context.getString(style.textRes)
        textView.setTextColor(ContextCompat.getColor(context, style.textColor))
        textView.setBackgroundResource(R.drawable.bg_status_badge)
        textView.background.setTint(ContextCompat.getColor(context, style.bgColor))
    }
}
