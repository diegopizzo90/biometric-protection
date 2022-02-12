package com.diegopizzo.biometricprotection

import android.content.Context
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import java.security.GeneralSecurityException
import java.util.concurrent.Executor

interface IBiometricProtectionManager {

    /**
     * Check if the biometric authentication is available
     * @param androidContext
     * @see [androidx.biometric.BiometricManager.canAuthenticate]
     */
    fun isBiometricUsable(androidContext: Context): Boolean

    /**
     * Setup of biometric authentication and the encryption mechanism to protect the pass-code
     * @param dataToEncrypt Data string to encrypt
     * @return data encrypted in byte array
     */
    fun biometricAuthenticationEnrollment(dataToEncrypt: String): EncryptedData?

    /**
     * Build biometric prompt dialog and add information showed to the user on the biometric authentication
     * @param titleRes Title resource displayed into dialog (Mandatory)
     * @param subtitleRes Subtitle resource displayed into dialog (Optional)
     * @param descriptionRes Description resource displayed into dialog (Optional)
     * @param negativeButtonTextRes Negative button text resource displayed into dialog (Mandatory)
     */
    fun buildBiometricPromptInfo(
        titleRes: Int,
        subtitleRes: Int? = null,
        descriptionRes: Int? = null,
        negativeButtonTextRes: Int,
        context: Context
    ): BiometricPrompt.PromptInfo

    /**
     * Create biometric prompt object
     * @param fragmentActivity Android FragmentActivity
     * @param executor Object used to execute the authentication on new thread
     * @param onSuccess Function executed in the [BiometricPrompt.AuthenticationCallback.onAuthenticationSucceeded] callback
     * @param onNegativeButtonClicked Function executed in the [BiometricPrompt.AuthenticationCallback.onAuthenticationError] callback when user click on negative button
     * @param onFailed Function executed in the [BiometricPrompt.AuthenticationCallback.onAuthenticationFailed] callback
     * @param onNoBiometricsEnrolled Function executed in the [BiometricPrompt.AuthenticationCallback.onAuthenticationError] callback when user doesn't enrolled any biometrics
     */
    fun createBiometricPromptListener(
        fragmentActivity: FragmentActivity,
        executor: Executor,
        encryptedData: EncryptedData,
        onSuccess: (String) -> Unit,
        onNegativeButtonClicked: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null,
        onNoBiometricsEnrolled: (() -> Unit)? = null
    ): BiometricPrompt

    /**
     * Start the authentication and show to the user the dialog with biometric checking
     * @param biometricPrompt [BiometricPrompt] object that wrap the callbacks for each scenario (Error, Success, Failed)
     * @param promptInfo [BiometricPrompt.PromptInfo] contains the text displayed in the system-provided biometric dialog
     */
    @Throws(GeneralSecurityException::class)
    @RequiresApi(23)
    fun showAuthenticationPrompt(
        biometricPrompt: BiometricPrompt,
        promptInfo: BiometricPrompt.PromptInfo,
        initVector: ByteArray
    )


    data class EncryptedData(val textEncrypted: ByteArray, val initVector: ByteArray)
}

/**
 * Get BiometricProtectionManager instance
 */
fun getBiometricProtectionInstance(): IBiometricProtectionManager {
    return BiometricProtectionManager()
}