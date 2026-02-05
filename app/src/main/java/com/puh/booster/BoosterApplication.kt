package com.puh.booster

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import dev.rikka.shizuku.Shizuku

class BoosterApplication : Application(), Configuration.Provider {
    
    companion object {
        lateinit var instance: BoosterApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize Shizuku
        if (Shizuku.pingBinder()) {
            Shizuku.addBinderDiedListener {
                // Handle Shizuku service death
                // Could notify user or schedule retry
            }
        }
        
        // Initialize WorkManager
        WorkManager.initialize(this, workManagerConfiguration)
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
}