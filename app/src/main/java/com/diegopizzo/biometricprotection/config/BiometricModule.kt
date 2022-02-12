package com.diegopizzo.biometricprotection.config

import com.diegopizzo.biometricprotection.IBiometricProtectionManager
import com.diegopizzo.biometricprotection.getBiometricProtectionInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object BiometricModule {

    @Provides
    fun provideBiometricProtectionManager(): IBiometricProtectionManager {
        return getBiometricProtectionInstance()
    }
}