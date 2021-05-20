package com.diegopizzo.biometricprotection

import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diegopizzo.biometricprotection.IBiometricProtectionManager.EncryptedData
import com.diegopizzo.biometricprotection.ViewState.*
import java.util.concurrent.Executor

class ViewModel(private val biometricProtection: IBiometricProtectionManager) : ViewModel() {

    private val mutableLiveData: MutableLiveData<ViewState> = MutableLiveData()
    val liveData = mutableLiveData
    var encryptedData: EncryptedData? = null

    init {
        if (biometricProtection.isBiometricUsable()) {
            mutableLiveData.value = BiometricUsable
        } else {
            mutableLiveData.value = BiometricNotUsable
        }
    }

    fun encryptData(dataToEncrypt: String) {
        encryptedData = biometricProtection.biometricAuthenticationEnrollment(dataToEncrypt)
        mutableLiveData.value =
            if (encryptedData == null) OnDataEncryptedError else OnDataEncryptedSuccess(
                encryptedData!!
            )
    }

    fun showAuthenticationPrompt(
        fragmentActivity: FragmentActivity, executor: Executor,
        encryptedData: EncryptedData, onSuccess: (String) -> Unit
    ) {
        biometricProtection.showAuthenticationPrompt(
            setUpPrompt(fragmentActivity, executor, encryptedData, onSuccess),
            setUpPromptInfo(),
            encryptedData.initVector
        )
    }

    fun onBiometricVerificationPassed(plainText: String) {
        liveData.value = OnDataDecryptedSuccess(plainText)
    }

    private fun setUpPromptInfo(): BiometricPrompt.PromptInfo {
        return biometricProtection.buildBiometricPromptInfo(
            titleRes = R.string.biometric_prompt_title,
            negativeButtonTextRes = R.string.biometric_prompt_negative_button
        )
    }

    private fun setUpPrompt(
        fragmentActivity: FragmentActivity, executor: Executor,
        encryptedData: EncryptedData, onSuccess: (String) -> Unit
    ): BiometricPrompt {
        return biometricProtection.createBiometricPromptListener(
            fragmentActivity, executor, encryptedData, onSuccess, promptInfo = setUpPromptInfo()
        )
    }
}

sealed class ViewState {
    object BiometricUsable : ViewState()
    object BiometricNotUsable : ViewState()
    class OnDataEncryptedSuccess(val encryptedData: EncryptedData) : ViewState()
    object OnDataEncryptedError : ViewState()
    class OnDataDecryptedSuccess(val dataDecrypted: String) : ViewState()
}