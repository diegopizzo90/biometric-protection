package com.diegopizzo.biometricprotection

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.diegopizzo.biometricprotection.IBiometricProtectionManager.EncryptedData
import com.diegopizzo.biometricprotection.databinding.ActivityMainBinding
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: ViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeViewState()
        setClickListeners()
        encrypt()
    }

    private fun setClickListeners() {
        binding.encryptButton.setOnClickListener { encrypt() }

        binding.decryptButton.setOnClickListener {
            viewModel.encryptedData?.let { encryptedData ->
                val executor = Executors.newSingleThreadExecutor()
                viewModel.showAuthenticationPrompt(
                    this, executor, encryptedData,
                    onSuccess = { textDecrypted ->
                        viewModel.onBiometricVerificationPassed(textDecrypted)
                    })
            }
        }
    }

    private fun encrypt() {
        viewModel.encryptData(binding.editTextTextMultiLine.text.toString())
    }

    private fun onDataEncrypted(encryptedData: EncryptedData) {
        binding.editTextTextMultiLine.setText(encryptedData.textEncrypted.toString())
        binding.encryptButton.isEnabled = false
        binding.decryptButton.isEnabled = true
    }

    private fun onDataDecrypted(textDecrypted: String) {
        binding.editTextTextMultiLine.setText(textDecrypted)
        binding.encryptButton.isEnabled = true
        binding.decryptButton.isEnabled = false
    }

    private fun showDialog(messageRes: Int, onButtonClick: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_error_title)
            .setMessage(messageRes)
            .setPositiveButton(R.string.dialog_error_title) { _, _ -> onButtonClick() }
    }

    private fun observeViewState() {
        viewModel.liveData.observe(this, {
            when (it) {
                ViewState.BiometricNotUsable -> {
                    showDialog(R.string.dialog_encrypt_error_biometric_not_usable) { finish() }
                }
                is ViewState.OnDataDecryptedSuccess -> onDataDecrypted(it.dataDecrypted)
                is ViewState.OnDataEncryptedSuccess -> onDataEncrypted(it.encryptedData)
                ViewState.OnDataEncryptedError -> {
                    showDialog(R.string.dialog_encrypt_error_message) { finish() }
                }
            }
        })
    }
}