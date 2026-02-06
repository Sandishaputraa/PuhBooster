package com.puh.booster

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import java.io.DataOutputStream
import java.io.IOException

object BoosterUtils {
    
    // Your existing code here...
    // Pastikan semua reference ke Shizuku pakai "rikka.shizuku.Shizuku"
    
    // Contoh fungsi yang menggunakan Shizuku
    fun checkShizukuPermission(): Boolean {
        return Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    
    fun requestShizukuPermission() {
        Shizuku.requestPermission(1000)
    }
}
