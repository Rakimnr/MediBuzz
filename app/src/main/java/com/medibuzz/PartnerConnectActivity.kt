package com.medibuzz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medibuzz.databinding.ActivityPartnerConnectBinding
import com.medibuzz.firebase.FirebaseAuthRepository
import com.medibuzz.firebase.PartnerRepository
import com.medibuzz.firebase.UserRole
import kotlinx.coroutines.launch

/**
 * Medicine User: shows partner code for sharing.
 * Care Partner: enter partner code to connect.
 */
class PartnerConnectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPartnerConnectBinding
    private lateinit var authRepository: FirebaseAuthRepository
    private lateinit var partnerRepository: PartnerRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPartnerConnectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = FirebaseAuthRepository(this)
        partnerRepository = PartnerRepository()

        binding.btnContinue.setOnClickListener { navigateHome() }

        lifecycleScope.launch {
            val profile = authRepository.getUserProfile()
            if (profile == null) {
                Toast.makeText(this@PartnerConnectActivity, R.string.error_profile, Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            when (profile.role) {
                UserRole.MEDICINE_USER -> showMedicineUserUi(profile.partnerCode ?: "")
                UserRole.CARE_PARTNER -> showCarePartnerUi()
            }
        }

        binding.btnConnect.setOnClickListener { connectPartner() }
    }

    private fun showMedicineUserUi(partnerCode: String) {
        binding.layoutMedicineUser.visibility = View.VISIBLE
        binding.layoutCarePartner.visibility = View.GONE
        binding.tvPartnerCode.text = partnerCode
        binding.tvPrivacyNote.text = getString(R.string.privacy_sharing_note)
    }

    private fun showCarePartnerUi() {
        binding.layoutMedicineUser.visibility = View.GONE
        binding.layoutCarePartner.visibility = View.VISIBLE
        binding.tvPrivacyNote.text = getString(R.string.privacy_sharing_note)
    }

    private fun connectPartner() {
        val code = binding.etPartnerCode.text.toString().trim()
        if (code.isEmpty()) {
            Toast.makeText(this, R.string.error_partner_code, Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnConnect.isEnabled = false

        lifecycleScope.launch {
            val result = partnerRepository.connectPartner(code)
            binding.progressBar.visibility = View.GONE
            binding.btnConnect.isEnabled = true

            result.onSuccess {
                Toast.makeText(this@PartnerConnectActivity, R.string.partner_connected, Toast.LENGTH_SHORT).show()
                navigateHome()
            }.onFailure { e ->
                Toast.makeText(
                    this@PartnerConnectActivity,
                    e.message ?: getString(R.string.error_connect_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun navigateHome() {
        lifecycleScope.launch {
            AuthNavigator.navigateToHome(this@PartnerConnectActivity)
        }
    }
}
