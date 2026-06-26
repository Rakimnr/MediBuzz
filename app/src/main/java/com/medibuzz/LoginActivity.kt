package com.medibuzz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medibuzz.databinding.ActivityLoginBinding
import com.medibuzz.firebase.FirebaseAuthRepository
import kotlinx.coroutines.launch

/**
 * Login screen with email and password.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authRepository: FirebaseAuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = FirebaseAuthRepository(this)

        // If already logged in, go to home
        if (authRepository.isLoggedIn) {
            lifecycleScope.launch { AuthNavigator.navigateToHome(this@LoginActivity) }
            return
        }

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.btnContinueOffline.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.error_login_fields, Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            val result = authRepository.login(email, password)
            binding.progressBar.visibility = View.GONE
            binding.btnLogin.isEnabled = true

            result.onSuccess {
                AuthNavigator.navigateToHome(this@LoginActivity)
            }.onFailure { e ->
                Toast.makeText(
                    this@LoginActivity,
                    e.message ?: getString(R.string.error_login_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
