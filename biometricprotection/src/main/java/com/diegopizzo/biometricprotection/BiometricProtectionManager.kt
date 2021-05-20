package com.diegopizzo.biometricprotection

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON
import androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED
import androidx.fragment.app.FragmentActivity
import com.diegopizzo.biometricprotection.IBiometricProtectionManager.EncryptedData
import java.nio.charset.Charset
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.UnrecoverableKeyException
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

internal class BiometricProtectionManager(private val context: Context) :
    IBiometricProtectionManager {

    override fun isBiometricUsable(): Boolean {
        return BiometricManager.from(context)
            .canAuthenticate(DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    override fun biometricAuthenticationEnrollment(dataToEncrypt: String): EncryptedData? {
        generateSecretKey(keyGenParameterSpec())
        return try {
            encryptInformation(dataToEncrypt)
        } catch (e: Exception) {
            null
        }
    }

    override fun buildBiometricPromptInfo(
        titleRes: Int, subtitleRes: Int?,
        descriptionRes: Int?, negativeButtonTextRes: Int
    ): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(titleRes))
            .setSubtitle(subtitleRes?.let { context.getString(it) })
            .setDescription(descriptionRes?.let { context.getString(it) })
            .setNegativeButtonText(context.getString(negativeButtonTextRes))
            .build()
    }

    override fun createBiometricPromptListener(
        fragmentActivity: FragmentActivity, executor: Executor,
        encryptedData: EncryptedData, onSuccess: (String) -> Unit,
        onNegativeButtonClicked: (() -> Unit)?, onFailed: (() -> Unit)?,
        promptInfo: BiometricPrompt.PromptInfo
    ): BiometricPrompt {
        return BiometricPrompt(fragmentActivity, executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    //When an unrecoverable error has been encountered and the authentication process
                    //has completed without success, then this callback will be triggered
                    super.onAuthenticationError(errorCode, errString)
                    //Run the callback when the user click on the negative button
                    if (errorCode == ERROR_NEGATIVE_BUTTON || errorCode == ERROR_USER_CANCELED) {
                        fragmentActivity.runOnUiThread { onNegativeButtonClicked?.let { it() } }
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    //The fingerprint/iris/face is has been successfully matched with one of the fingerprints/iris/faces registered on the device
                    super.onAuthenticationSucceeded(result)
                    try {
                        val decryptedData =
                            decryptInformation(result.cryptoObject?.cipher!!, encryptedData)
                        fragmentActivity.runOnUiThread { onSuccess(decryptedData) }
                    } catch (e: Exception) {
                        onAuthenticationError(ERROR_NEGATIVE_BUTTON, "")
                    }
                }

                override fun onAuthenticationFailed() {
                    //The fingerprint/iris/face does not match with any of the fingerprints/iris/faces registered on the device
                    super.onAuthenticationFailed()
                    fragmentActivity.runOnUiThread { onFailed?.let { it() } }
                }
            })
    }

    override fun showAuthenticationPrompt(
        biometricPrompt: BiometricPrompt,
        promptInfo: BiometricPrompt.PromptInfo,
        initVector: ByteArray
    ) {
        val cipher = prepareDecryptCipher(initVector)
        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    private fun encryptInformation(stringToEncrypt: String): EncryptedData {
        val cipher = getCipher()
        val secretKey = getSecretKey()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val dataEncrypted = cipher.doFinal(stringToEncrypt.toByteArray(Charset.defaultCharset()))
        return EncryptedData(dataEncrypted, cipher.iv)
    }

    private fun decryptInformation(cipher: Cipher, encryptedData: EncryptedData): String {
        return cipher.doFinal(encryptedData.textEncrypted).toString(Charsets.UTF_8)
    }

    private fun prepareDecryptCipher(initializationVector: ByteArray): Cipher {
        val cipher = getCipher()
        val secretKey = getSecretKey()
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(initializationVector))
        return cipher
    }

    private fun keyGenParameterSpec() = KeyGenParameterSpec.Builder(
        KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setKeySize(256)
        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
        .build()

    private fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, KEY_STORE
        )
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    private fun getSecretKey(retry: Boolean = true): SecretKey {
        val keyStore = KeyStore.getInstance(KEY_STORE)
        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null)
        return try {
            keyStore.getKey(KEY_ALIAS, null) as SecretKey
        } catch (e: UnrecoverableKeyException) {
            if (retry) {
                keyStore.deleteEntry(KEY_ALIAS)
                getSecretKey(false)
            } else {
                throw GeneralSecurityException()
            }
        }
    }

    private fun getCipher(): Cipher {
        return Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7
        )
    }

    companion object {
        private const val KEY_STORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "BiometricKey"
    }
}
