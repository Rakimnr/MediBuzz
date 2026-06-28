package com.medibuzz

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.medibuzz.databinding.ActivityChooseRoleBinding
import com.medibuzz.firebase.UserRole

class ChooseRoleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChooseRoleBinding
    private var selectedRole: UserRole = UserRole.MEDICINE_USER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseRoleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardMedicineUser.setOnClickListener {
            selectedRole = UserRole.MEDICINE_USER
            updateSelection()
        }
        binding.cardCarePartner.setOnClickListener {
            selectedRole = UserRole.CARE_PARTNER
            updateSelection()
        }

        binding.btnConfirmRole.setOnClickListener { returnSelectedRole() }
        updateSelection()
    }

    private fun updateSelection() {
        binding.cardMedicineUser.alpha = if (selectedRole == UserRole.MEDICINE_USER) 1.0f else 0.6f
        binding.cardCarePartner.alpha = if (selectedRole == UserRole.CARE_PARTNER) 1.0f else 0.6f
    }

    private fun returnSelectedRole() {
        val intent = Intent().apply {
            putExtra("role", selectedRole.name)
        }
        setResult(RESULT_OK, intent)
        finish()
    }
}
