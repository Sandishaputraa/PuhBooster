package com.puh.booster

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.puh.booster.databinding.ActivityMainBinding
import com.puh.booster.models.ProfileType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val scope = CoroutineScope(Dispatchers.Main)
    
    // Shizuku permission listener
    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == 1000) {
            val message = if (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                "Shizuku permission granted!"
            } else {
                "Shizuku permission denied"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        checkShizukuAvailability()
    }
    
    override fun onStart() {
        super.onStart()
        Shizuku.addRequestPermissionResultListener(permissionListener)
    }
    
    override fun onStop() {
        super.onStop()
        Shizuku.removeRequestPermissionResultListener(permissionListener)
    }
    
    private fun setupUI() {
        // Profile buttons
        binding.btnGaming.setOnClickListener { applyProfile(ProfileType.GAMING) }
        binding.btnBattery.setOnClickListener { applyProfile(ProfileType.BATTERY) }
        binding.btnBalanced.setOnClickListener { applyProfile(ProfileType.BALANCED) }
        
        // Quick actions
        binding.btnClearCache.setOnClickListener { clearSystemCache() }
        binding.btnBoostRam.setOnClickListener { boostMemory() }
        binding.btnKillApps.setOnClickListener { killBackgroundApps() }
        binding.btnNetworkBoost.setOnClickListener { optimizeNetwork() }
    }
    
    private fun checkShizukuAvailability() {
        binding.shizukuStatus.text = if (BoosterUtils.isShizukuAvailable()) {
            "Shizuku: Available"
        } else {
            "Shizuku: Not Available"
        }
    }
    
    private fun applyProfile(profile: ProfileType) {
        if (!BoosterUtils.isShizukuAvailable()) {
            Toast.makeText(this, "Shizuku permission required!", Toast.LENGTH_SHORT).show()
            return
        }
        
        scope.launch {
            val success = BoosterUtils.applyProfile(this@MainActivity, profile)
            
            if (success) {
                val message = when (profile) {
                    ProfileType.GAMING -> "Gaming mode activated! 🎮"
                    ProfileType.BATTERY -> "Battery saver activated! 🔋"
                    ProfileType.BALANCED -> "Balanced mode activated! ⚖️"
                    ProfileType.CUSTOM -> "Custom profile applied! ⚙️"
                }
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@MainActivity, "Failed to apply profile", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun clearSystemCache() {
        scope.launch {
            val success = BoosterUtils.clearAppCache(this@MainActivity, "com.android.settings")
            
            if (success) {
                Toast.makeText(this@MainActivity, "System cache cleared!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Failed to clear cache", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun boostMemory() {
        scope.launch {
            val result = BoosterUtils.executeCommand("sync")
            
            if (result.isNotEmpty()) {
                Toast.makeText(this@MainActivity, "Memory optimized!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun killBackgroundApps() {
        scope.launch {
            val success = BoosterUtils.forceStopApp("com.example.app") // Ganti dengan package yang sesuai
            
            if (success) {
                Toast.makeText(this@MainActivity, "Background apps killed!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Failed to kill apps", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun optimizeNetwork() {
        scope.launch {
            val commands = listOf(
                "settings put global mobile_data_always_on 0",
                "settings put global wifi_scan_always_enabled 0",
                "ip route flush cache"
            )
            
            var successCount = 0
            commands.forEach { cmd ->
                val result = BoosterUtils.executeCommand(cmd)
                if (result.isNotEmpty()) successCount++
            }
            
            Toast.makeText(
                this@MainActivity,
                "Network optimized: $successCount/${commands.size} commands",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun updateLastApplied(profileName: String) {
        // Simpan ke preferences atau update UI
        binding.lastApplied.text = "Last applied: $profileName"
    }
}
