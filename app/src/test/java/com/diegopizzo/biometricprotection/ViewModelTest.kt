package com.diegopizzo.biometricprotection

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.diegopizzo.biometricprotection.IBiometricProtectionManager.EncryptedData
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ViewModel

    @Mock
    private lateinit var biometricManager: IBiometricProtectionManager

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var stateObserver: Observer<ViewState>

    @Before
    fun setUp() {
        viewModel = ViewModel(biometricManager).apply {
            liveData.observeForever(stateObserver)
        }
    }

    @Test
    fun checkIfBiometricIsUsable_isNotUsable_verifyValue() {
        `when`(biometricManager.isBiometricUsable(context)).thenReturn(false)
        viewModel.checkIfBiometricIsUsable(context)
        verify(stateObserver).onChanged(ViewState.BiometricNotUsable)
    }

    @Test
    fun encryptData_dataEncrypted_verifyValue() {
        `when`(biometricManager.biometricAuthenticationEnrollment(anyString())).thenReturn(
            EncryptedData(byteArrayOf(Byte.MIN_VALUE), byteArrayOf(Byte.MIN_VALUE))
        )
        viewModel.encryptData(anyString())
        verify(stateObserver).onChanged(isA(ViewState.OnDataEncryptedSuccess::class.java))
    }

    @Test
    fun encryptData_error_verifyValue() {
        `when`(biometricManager.biometricAuthenticationEnrollment(anyString())).thenReturn(null)
        viewModel.encryptData(anyString())
        verify(stateObserver).onChanged(ViewState.OnDataEncryptedError)
    }
}