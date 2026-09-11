package com.example.photogallery

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.photogallery.databinding.ActivityAppLockBinding
import com.example.photogallery.utils.SecureStorage
import java.util.concurrent.Executor

class AppLockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppLockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val savedSecret = SecureStorage.getSecret(this)

        if (savedSecret == null) {
            binding.tvLockTitle.text = "Create App PIN"
            binding.btnUnlock.text = "Save PIN"
            binding.layoutConfirmPin.visibility = View.VISIBLE
        } else {
            binding.tvLockTitle.text = "Enter App PIN"
            binding.btnUnlock.text = "Unlock"
            binding.layoutConfirmPin.visibility = View.GONE
            if (SecureStorage.isBiometricEnabled(this)) showBiometricPrompt()
        }

        binding.btnUnlock.setOnClickListener {
            val pin = binding.etPin.text.toString().trim()
            if (savedSecret == null) {
                val confirm = binding.etConfirmPin.text.toString().trim()
                if (pin.length >= 4 && pin == confirm) {
                    SecureStorage.saveCredentials(this, pin, "PIN")
                    SecureStorage.setAppLockEnabled(this, true)
                    navigateToMain()
                } else {
                    Toast.makeText(this, "Invalid PIN or mismatch", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (pin == savedSecret) navigateToMain()
                else Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBiometric.setOnClickListener { showBiometricPrompt() }
    }

    private fun showBiometricPrompt() {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                navigateToMain()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Toast.makeText(applicationContext, "Error: $errString", Toast.LENGTH_SHORT).show()
            }
        })
        prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("Unlock App").setNegativeButtonText("Use PIN").build())
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}