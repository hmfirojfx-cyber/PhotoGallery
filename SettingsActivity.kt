package com.example.photogallery

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.photogallery.databinding.ActivitySettingsBinding
import com.example.photogallery.utils.SecureStorage

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.switchAppLock.isChecked = SecureStorage.isAppLockEnabled(this)
        binding.switchBiometric.isChecked = SecureStorage.isBiometricEnabled(this)

        binding.switchAppLock.setOnCheckedChangeListener { _, isChecked ->
            SecureStorage.setAppLockEnabled(this, isChecked)
        }

        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            SecureStorage.setBiometricEnabled(this, isChecked)
        }
    }
}