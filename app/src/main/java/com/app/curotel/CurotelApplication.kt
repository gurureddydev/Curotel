package com.app.curotel

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Curotel - Entry point for Hilt dependency injection
 */
@HiltAndroidApp
class CurotelApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide configs here
    }
}
