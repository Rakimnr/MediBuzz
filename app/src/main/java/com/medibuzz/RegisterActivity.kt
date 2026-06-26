package com.medibuzz

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.medibuzz.databinding.ActivityRegisterBinding

/**
 * Registration screen — collects email, password, and display name.
 * Role selection happens in ChooseRoleActivity.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener { attemptRegister() }
        binding.btnBackToLogin.setOnClickListener { finish() }
    }

    private fun attemptRegister() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val displayName = binding.etDisplayName.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || displayName.isEmpty()) {
            android.widget.Toast.makeText(this, R.string.error_register_fields, android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            android.widget.Toast.makeText(this, R.string.error_password_length, android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ChooseRoleActivity::class.java).apply {
            putExtra(ChooseRoleActivity.EXTRA_EMAIL, email)
            putExtra(ChooseRoleActivity.EXTRA_PASSWORD, password)
            putExtra(ChooseRoleActivity.EXTRA_DISPLAY_NAME, displayName)
        }
        startActivity(intent)
    }
}
