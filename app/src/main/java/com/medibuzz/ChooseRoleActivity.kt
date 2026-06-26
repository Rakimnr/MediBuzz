package com.medibuzz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medibuzz.databinding.ActivityChooseRoleBinding
import com.medibuzz.firebase.FirebaseAuthRepository
import com.medibuzz.firebase.UserRole
import kotlinx.coroutines.launch

/**
 * Lets the user choose Medicine User or Care Partner role after registration.
 */
class ChooseRoleActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EMAIL = "extra_email"
        const val EXTRA_PASSWORD = "extra_password"
        const val EXTRA_DISPLAY_NAME = "extra_display_name"
    }

    private lateinit var binding: ActivityChooseRoleBinding
    private lateinit var authRepository: FirebaseAuthRepository
    private var selectedRole: UserRole = UserRole.MEDICINE_USER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseRoleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = FirebaseAuthRepository(this)

        binding.cardMedicineUser.setOnClickListener {
            selectedRole = UserRole.MEDICINE_USER
            updateSelection()
        }
        binding.cardCarePartner.setOnClickListener {
            selectedRole = UserRole.CARE_PARTNER
            updateSelection()
        }

        binding.btnConfirmRole.setOnClickListener { completeRegistration() }
        updateSelection()
    }

    private fun updateSelection() {
        binding.cardMedicineUser.alpha = if (selectedRole == UserRole.MEDICINE_USER) 1.0f else 0.6f
        binding.cardCarePartner.alpha = if (selectedRole == UserRole.CARE_PARTNER) 1.0f else 0.6f
    }

    private fun completeRegistration() {
        val email = intent.getStringExtra(EXTRA_EMAIL) ?: return
        val password = intent.getStringExtra(EXTRA_PASSWORD) ?: return
        val displayName = intent.getStringExtra(EXTRA_DISPLAY_NAME) ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.btnConfirmRole.isEnabled = false

        lifecycleScope.launch {
            val result = authRepository.register(email, password, displayName, selectedRole)
            binding.progressBar.visibility = View.GONE
            binding.btnConfirmRole.isEnabled = true

            result.onSuccess {
                when (selectedRole) {
                    UserRole.MEDICINE_USER -> {
                        startActivity(Intent(this@ChooseRoleActivity, PartnerConnectActivity::class.java))
                    }
                    UserRole.CARE_PARTNER -> {
                        startActivity(Intent(this@ChooseRoleActivity, PartnerConnectActivity::class.java))
                    }
                }
                finish()
            }.onFailure { e ->
                Toast.makeText(
                    this@ChooseRoleActivity,
                    e.message ?: getString(R.string.error_register_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
