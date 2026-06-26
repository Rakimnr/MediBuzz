package com.medibuzz

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medibuzz.databinding.ActivitySharingSettingsBinding
import com.medibuzz.firebase.FirebaseAuthRepository
import com.medibuzz.firebase.PartnerLink
import com.medibuzz.firebase.PartnerRepository
import kotlinx.coroutines.launch

/**
 * Medicine user controls partner sharing settings.
 */
class SharingSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySharingSettingsBinding
    private lateinit var authRepository: FirebaseAuthRepository
    private lateinit var partnerRepository: PartnerRepository
    private var currentLink: PartnerLink? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySharingSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = FirebaseAuthRepository(this)
        partnerRepository = PartnerRepository()

        binding.tvPrivacyNote.text = getString(R.string.privacy_sharing_note)
        binding.btnBack.setOnClickListener { finish() }
        binding.btnRemovePartner.setOnClickListener { removePartner() }

        loadSettings()
    }

    private fun loadSettings() {
        lifecycleScope.launch {
            val uid = authRepository.currentUser?.uid ?: return@launch
            val profile = authRepository.getUserProfile()
            val link = partnerRepository.getPartnerLinkForMedicineUser(uid)
            currentLink = link

            binding.tvPartnerCode.text = profile?.partnerCode ?: ""

            if (link != null) {
                val partnerProfile = authRepository.getUserProfileById(link.carePartnerId)
                binding.tvConnectedPartner.text = partnerProfile?.email ?: link.carePartnerId
                binding.layoutConnectedPartner.visibility = View.VISIBLE
                binding.switchSharing.isEnabled = true
                binding.switchSharing.setOnCheckedChangeListener(null)
                binding.switchSharing.isChecked = link.sharingEnabled
                binding.switchSharing.setOnCheckedChangeListener { _, isChecked ->
                    toggleSharing(isChecked)
                }
                binding.btnRemovePartner.visibility = View.VISIBLE
            } else {
                binding.tvConnectedPartner.text = getString(R.string.no_partner_connected)
                binding.layoutConnectedPartner.visibility = View.VISIBLE
                binding.switchSharing.isEnabled = false
                binding.btnRemovePartner.visibility = View.GONE
            }
        }
    }

    private fun toggleSharing(enabled: Boolean) {
        val link = currentLink ?: return
        lifecycleScope.launch {
            val result = partnerRepository.setSharingEnabled(link.id, enabled)
            result.onSuccess {
                if (enabled) {
                    com.medibuzz.firebase.FirestoreSyncRepository(this@SharingSettingsActivity).syncTodayScheduleIfEnabled()
                }
            }.onFailure { e ->
                binding.switchSharing.isChecked = !enabled
                Toast.makeText(this@SharingSettingsActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removePartner() {
        val link = currentLink ?: return
        lifecycleScope.launch {
            val result = partnerRepository.removePartnerLink(link.id)
            result.onSuccess {
                Toast.makeText(this@SharingSettingsActivity, R.string.partner_removed, Toast.LENGTH_SHORT).show()
                currentLink = null
                loadSettings()
            }.onFailure { e ->
                Toast.makeText(this@SharingSettingsActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
