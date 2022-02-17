package com.diegopizzo.biometricprotection

import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

class TestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        name: String?,
        context: Context?
    ): android.app.Application? {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}