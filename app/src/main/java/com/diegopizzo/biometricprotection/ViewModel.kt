package com.diegopizzo.biometricprotection

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diegopizzo.biometricprotection.IBiometricProtectionManager.EncryptedData
import com.diegopizzo.biometricprotection.ViewState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.Executor
import javax.inject.Inject

@HiltViewModel
class ViewModel @Inject constructor(private val biometricProtection: IBiometricProtectionManager) :
    ViewModel() {

    private val mutableLiveData: MutableLiveData<ViewState> = MutableLiveData()
    val liveData = mutableLiveData
    var encryptedData: EncryptedData? = null

    fun checkIfBiometricIsUsable(context: Context) {
        if (!biometricProtection.isBiometricUsable(context)) {
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
        encryptedData: EncryptedData, onSuccess: (String) -> Unit,
        onNoBiometricsEnrolled: () -> Unit,
        context: Context
    ) {
        biometricProtection.showAuthenticationPrompt(
            setUpBiometricPrompt(
                fragmentActivity, executor, encryptedData, onSuccess,
                onNoBiometricsEnrolled
            ),
            setUpPromptInfo(context),
            encryptedData.initVector
        )
    }

    fun onBiometricVerificationPassed(plainText: String) {
        liveData.value = OnDataDecryptedSuccess(plainText)
    }

    private fun setUpPromptInfo(context: Context): BiometricPrompt.PromptInfo {
        return biometricProtection.buildBiometricPromptInfo(
            titleRes = R.string.biometric_prompt_title,
            negativeButtonTextRes = R.string.biometric_prompt_negative_button,
            context = context
        )
    }

    private fun setUpBiometricPrompt(
        fragmentActivity: FragmentActivity, executor: Executor,
        encryptedData: EncryptedData, onSuccess: (String) -> Unit,
        onNoBiometricsEnrolled: () -> Unit
    ): BiometricPrompt {
        return biometricProtection.createBiometricPromptListener(
            fragmentActivity, executor, encryptedData, onSuccess,
            onNoBiometricsEnrolled = onNoBiometricsEnrolled
        )
    }
}

sealed class ViewState {
    object BiometricNotUsable : ViewState()
    class OnDataEncryptedSuccess(val encryptedData: EncryptedData) : ViewState()
    object OnDataEncryptedError : ViewState()
    class OnDataDecryptedSuccess(val dataDecrypted: String) : ViewState()
}