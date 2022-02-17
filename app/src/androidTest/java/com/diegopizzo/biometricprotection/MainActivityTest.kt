package com.diegopizzo.biometricprotection

import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.diegopizzo.biometricprotection.IBiometricProtectionManager.EncryptedData
import com.diegopizzo.biometricprotection.config.BiometricModule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@UninstallModules(BiometricModule::class)
@HiltAndroidTest
@RunWith(MockitoJUnitRunner::class)
class MainActivityTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @BindValue
    @Mock
    lateinit var biometricManager: IBiometricProtectionManager

    @Before
    fun setUp() {
        `when`(biometricManager.isBiometricUsable(context)).thenReturn(true)
        `when`(biometricManager.biometricAuthenticationEnrollment(anyString())).thenReturn(
            EncryptedData(byteArrayOf(anyByte()), byteArrayOf(anyByte()))
        )
    }

    @Test
    fun activityLaunched_checkUiInitialState() {
        launchActivity<MainActivity>()
        onView(withId(R.id.textView)).check(matches(withText(R.string.text_encrypted)))
        onView(withId(R.id.editTextTextMultiLine)).check(matches(not(isEnabled())))
        onView(withId(R.id.encrypt_button)).check(matches(withText(R.string.encrypt)))
        onView(withId(R.id.encrypt_button)).check(matches(not(isEnabled())))
        onView(withId(R.id.decrypt_button)).check(matches(withText(R.string.decrypt)))
        onView(withId(R.id.decrypt_button)).check(matches(isEnabled()))
    }
}