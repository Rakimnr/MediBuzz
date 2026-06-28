package com.medibuzz

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medibuzz.databinding.ActivityRegisterBinding
import com.medibuzz.firebase.FirebaseAuthRepository
import com.medibuzz.firebase.UserRole
import kotlinx.coroutines.launch

/**
 * Registration screen — collects email, password, and display name.
 * Role selection happens in ChooseRoleActivity via Activity Result.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authRepository: FirebaseAuthRepository

    private val chooseRoleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val roleStr = result.data?.getStringExtra("role") ?: return@registerForActivityResult
            val role = UserRole.safeValueOf(roleStr)
            performRegistration(role)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        authRepository = FirebaseAuthRepository(this)

        binding.btnRegister.setOnClickListener { attemptRegister() }
        binding.btnBackToLogin.setOnClickListener { finish() }
    }

    private fun attemptRegister() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val displayName = binding.etDisplayName.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || displayName.isEmpty()) {
            Toast.makeText(this, R.string.error_register_fields, Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, R.string.error_password_length, Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ChooseRoleActivity::class.java)
        chooseRoleLauncher.launch(intent)
    }

    private fun performRegistration(role: UserRole) {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val displayName = binding.etDisplayName.text.toString().trim()

        // Hide register button or show a progress bar if available in UI. 
        binding.btnRegister.isEnabled = false

        lifecycleScope.launch {
            val result = authRepository.register(email, password, displayName, role)
            binding.btnRegister.isEnabled = true

            result.onSuccess {
                startActivity(Intent(this@RegisterActivity, PartnerConnectActivity::class.java))
                finish()
            }.onFailure { e ->
                Toast.makeText(
                    this@RegisterActivity,
                    e.message ?: getString(R.string.error_register_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
