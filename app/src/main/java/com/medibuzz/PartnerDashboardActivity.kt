package com.medibuzz

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.medibuzz.databinding.ActivityPartnerDashboardBinding
import com.medibuzz.firebase.FirebaseAuthRepository
import com.medibuzz.firebase.FirestoreSyncRepository
import com.medibuzz.firebase.PartnerRepository
import com.medibuzz.firebase.SharedStatus
import com.medibuzz.data.ReminderStatus
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Care partner dashboard showing shared medicine statuses.
 */
class PartnerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPartnerDashboardBinding
    private lateinit var authRepository: FirebaseAuthRepository
    private lateinit var syncRepository: FirestoreSyncRepository
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private var adapter: PartnerStatusAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPartnerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = FirebaseAuthRepository(this)
        syncRepository = FirestoreSyncRepository(this)

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.btnReconnect.setOnClickListener {
            startActivity(Intent(this, PartnerConnectActivity::class.java))
        }

        loadDashboard()
    }

    override fun onResume() {
        super.onResume()
        loadDashboard()
    }

    private fun loadDashboard() {
        lifecycleScope.launch {
            val uid = authRepository.currentUser?.uid
            if (uid == null) {
                AuthNavigator.navigateToHome(this@PartnerDashboardActivity)
                return@launch
            }

            val profile = authRepository.getUserProfile()
            binding.tvGreeting.text = getString(R.string.greeting_partner, profile?.displayName ?: "")

            try {
                val link = PartnerRepository().getPartnerLinkForCarePartner(uid)
                if (link == null) {
                    showNoConnection()
                    return@launch
                }
                
                binding.btnReconnect.visibility = View.GONE

                if (!link.sharingEnabled) {
                    binding.tvSharingDisabled.visibility = View.VISIBLE
                    binding.rvPartnerStatus.visibility = View.GONE
                    binding.layoutSummary.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.tvEmptyState.text = getString(R.string.sharing_disabled_partner)
                    return@launch
                }

                binding.tvSharingDisabled.visibility = View.GONE

                val statuses = syncRepository.getTodaySharedStatusesForPartner(uid)
                if (statuses.isEmpty()) {
                    binding.rvPartnerStatus.visibility = View.GONE
                    binding.layoutSummary.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.tvEmptyState.text = getString(R.string.partner_no_status)
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvPartnerStatus.visibility = View.VISIBLE
                    binding.layoutSummary.visibility = View.VISIBLE
                    updateSummary(statuses)
                    
                    if (adapter == null) {
                        adapter = PartnerStatusAdapter(statuses, timeFormat)
                        binding.rvPartnerStatus.layoutManager = LinearLayoutManager(this@PartnerDashboardActivity)
                        binding.rvPartnerStatus.adapter = adapter
                    } else {
                        adapter?.updateItems(statuses)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PartnerDashboard", "Failed to load dashboard data", e)
                android.widget.Toast.makeText(this@PartnerDashboardActivity, "Failed to load dashboard", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showNoConnection() {
        binding.layoutSummary.visibility = View.GONE
        binding.rvPartnerStatus.visibility = View.GONE
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.tvEmptyState.text = getString(R.string.partner_not_connected)
        binding.btnReconnect.visibility = View.VISIBLE
    }

    private fun updateSummary(statuses: List<SharedStatus>) {
        val taken = statuses.count { it.status == ReminderStatus.TAKEN }
        val pending = statuses.count { it.status == ReminderStatus.PENDING || it.status == ReminderStatus.SNOOZED }
        val missed = statuses.count { it.status == ReminderStatus.MISSED }
        val skipped = statuses.count { it.status == ReminderStatus.SKIPPED }

        binding.tvSummaryTaken.text = taken.toString()
        binding.tvSummaryPending.text = pending.toString()
        binding.tvSummaryMissed.text = missed.toString()
        binding.tvSummarySkipped.text = skipped.toString()
    }
}
