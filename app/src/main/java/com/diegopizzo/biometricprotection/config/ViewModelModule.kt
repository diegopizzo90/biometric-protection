package com.diegopizzo.biometricprotection.config

import com.diegopizzo.biometricprotection.ViewModel
import com.diegopizzo.biometricprotection.getBiometricProtectionInstance
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        ViewModel(getBiometricProtectionInstance(androidContext()))
    }
}