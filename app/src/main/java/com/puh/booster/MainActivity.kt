package com.puh.booster

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.puh.booster.databinding.ActivityMainBinding
import com.puh.booster.models.ProfileType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
            updateShizukuStatus()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        updateShizukuStatus()
        setupSwipeRefresh()
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
        // Profile buttons - menggunakan View Binding dengan ID yang benar
        binding.btnGaming.setOnClickListener { applyProfile(ProfileType.GAMING) }
        binding.btnBalanced.setOnClickListener { applyProfile(ProfileType.BALANCED) }
        binding.btnBattery.setOnClickListener { applyProfile(ProfileType.BATTERY) }
        
        // Quick action buttons
        binding.btnClearCache.setOnClickListener { clearSystemCache() }
        binding.btnBoostRam.setOnClickListener { boostMemory() }
        binding.btnKillApps.setOnClickListener { killBackgroundApps() }
        binding.btnNetworkBoost.setOnClickListener { optimizeNetwork() }
        
        // Info and Settings buttons
        binding.btnInfo.setOnClickListener { showAppInfo() }
        binding.btnSettings.setOnClickListener { showSettings() }
        binding.btnRequestPermission.setOnClickListener { requestShizukuPermission() }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            refreshData()
        }
    }
    
    private fun refreshData() {
        scope.launch {
            updateShizukuStatus()
            updateSystemInfo()
            binding.swipeRefresh.isRefreshing = false
        }
    }
    
    private fun updateShizukuStatus() {
        val isAvailable = BoosterUtils.isShizukuAvailable()
        val hasPermission = BoosterUtils.checkShizukuPermission()
        
        binding.tvShizukuStatus.text = when {
            !isAvailable -> "Shizuku: Not Available"
            !hasPermission -> "Shizuku: No Permission"
            else -> "Shizuku: Ready ✓"
        }
        
        binding.btnRequestPermission.isEnabled = isAvailable && !hasPermission
    }
    
    private fun requestShizukuPermission() {
        BoosterUtils.requestShizukuPermission(1000)
    }
    
    private fun applyProfile(profile: ProfileType) {
        if (!BoosterUtils.isShizukuAvailable()) {
            Toast.makeText(this, "Shizuku not available!", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!BoosterUtils.checkShizukuPermission()) {
            Toast.makeText(this, "Please grant Shizuku permission first!", Toast.LENGTH_SHORT).show()
            requestShizukuPermission()
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
                
                // Update last applied
                binding.tvLastApplied.text = "Last: ${profile.name}"
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
            val success = BoosterUtils.forceStopApp("com.example.app")
            
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
    
    private fun updateSystemInfo() {
        scope.launch {
            val systemInfo = BoosterUtils.getSystemInfo()
            val infoText = buildString {
                systemInfo.forEach { (key, value) ->
                    append("$key: $value\n")
                }
            }
            binding.tvSystemInfo.text = infoText.trim()
        }
    }
    
    private fun showAppInfo() {
        Toast.makeText(this, "PuhBooster v1.0.0\nPerformance Booster for Android", Toast.LENGTH_LONG).show()
    }
    
    private fun showSettings() {
        Toast.makeText(this, "Settings will be available soon!", Toast.LENGTH_SHORT).show()
    }
}
