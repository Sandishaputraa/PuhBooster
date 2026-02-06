package com.puh.booster

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import rikka.shizuku.Shizuku

class BoosterApplication : Application(), Configuration.Provider {
    
    companion object {
        lateinit var instance: BoosterApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize Shizuku - hanya jika service tersedia
        try {
            if (Shizuku.pingBinder()) {
                // Shizuku v13+ mungkin tidak punya addBinderDiedListener
                // Cukup ping saja untuk cek ketersediaan
            }
        } catch (e: Exception) {
            // Ignore if Shizuku not available
        }
        
        // Initialize WorkManager
        WorkManager.initialize(this, workManagerConfiguration)
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
