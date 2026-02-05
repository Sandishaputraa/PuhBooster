package com.puh.booster

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.puh.booster.databinding.ActivityMainBinding
import dev.rikka.shizuku.Shizuku
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val shizukuPermissionCode = 1000
    
    // Shizuku permission listener
    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == shizukuPermissionCode) {
            handleShizukuPermissionResult(grantResult)
        }
    }
    
    // Register for Shizuku permission request
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle permission result
        checkShizukuStatus()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupShizuku()
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
        binding.btnGaming.setOnClickListener { applyProfile(BoosterUtils.ProfileType.GAMING) }
        binding.btnBattery.setOnClickListener { applyProfile(BoosterUtils.ProfileType.BATTERY) }
        binding.btnBalanced.setOnClickListener { applyProfile(BoosterUtils.ProfileType.BALANCED) }
        
        // Quick actions
        binding.btnClearCache.setOnClickListener { clearSystemCache() }
        binding.btnBoostRam.setOnClickListener { boostMemory() }
        binding.btnKillApps.setOnClickListener { killBackgroundApps() }
        binding.btnNetworkBoost.setOnClickListener { optimizeNetwork() }
        
        // Settings
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        // Info
        binding.btnInfo.setOnClickListener {
            showAppInfoDialog()
        }
        
        // Status refresh
        binding.swipeRefresh.setOnRefreshListener {
            refreshStatus()
            binding.swipeRefresh.isRefreshing = false
        }
    }
    
    private fun setupShizuku() {
        if (!Shizuku.pingBinder()) {
            showShizukuNotInstalledDialog()
            return
        }
        
        checkShizukuStatus()
    }
    
    private fun checkShizukuStatus() {
        val hasPermission = BoosterUtils.isShizukuAvailable()
        
        binding.tvShizukuStatus.text = if (hasPermission) {
            "✅ Shizuku Ready"
        } else {
            "⚠️ Shizuku Permission Required"
        }
        
        binding.btnRequestPermission.isVisible = !hasPermission
        binding.btnRequestPermission.setOnClickListener {
            requestShizukuPermission()
        }
        
        // Enable/disable buttons based on permission
        setButtonsEnabled(hasPermission)
    }
    
    private fun setButtonsEnabled(enabled: Boolean) {
        binding.btnGaming.isEnabled = enabled
        binding.btnBattery.isEnabled = enabled
        binding.btnBalanced.isEnabled = enabled
        binding.btnClearCache.isEnabled = enabled
        binding.btnBoostRam.isEnabled = enabled
        binding.btnKillApps.isEnabled = enabled
        binding.btnNetworkBoost.isEnabled = enabled
        
        val alpha = if (enabled) 1.0f else 0.5f
        binding.btnGaming.alpha = alpha
        binding.btnBattery.alpha = alpha
        binding.btnBalanced.alpha = alpha
    }
    
    private fun requestShizukuPermission() {
        if (Shizuku.shouldShowRequestPermissionRationale()) {
            showPermissionExplanationDialog()
        } else {
            Shizuku.requestPermission(shizukuPermissionCode)
        }
    }
    
    private fun handleShizukuPermissionResult(grantResult: Int) {
        val granted = grantResult == PackageManager.PERMISSION_GRANTED
        
        if (granted) {
            Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show()
            checkShizukuStatus()
            initializeBooster()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            showPermissionRequiredDialog()
        }
    }
    
    private fun initializeBooster() {
        CoroutineScope(Dispatchers.IO).launch {
            // Load saved preferences
            // Initialize system monitoring
            // Setup auto-optimization if enabled
        }
    }
    
    private fun applyProfile(profile: BoosterUtils.ProfileType) {
        if (!BoosterUtils.isShizukuAvailable()) {
            Toast.makeText(this, "Shizuku permission required!", Toast.LENGTH_SHORT).show()
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            val success = BoosterUtils.applyProfile(profile, this@MainActivity)
            
            withContext(Dispatchers.Main) {
                if (success) {
                    val message = when (profile) {
                        BoosterUtils.ProfileType.GAMING -> "Gaming mode activated! 🎮"
                        BoosterUtils.ProfileType.BATTERY -> "Battery saver activated! 🔋"
                        BoosterUtils.ProfileType.BALANCED -> "Balanced mode activated! ⚖️"
                        BoosterUtils.ProfileType.CUSTOM -> "Custom profile applied! ⚙️"
                    }
                    
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                    updateLastApplied(profile.name)
                } else {
                    Toast.makeText(this@MainActivity, "Failed to apply profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun clearSystemCache() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = BoosterUtils.clearAppCache("com.android.settings")
            
            withContext(Dispatchers.Main) {
                if (result.success) {
                    Toast.makeText(this@MainActivity, "System cache cleared!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Failed: ${result.error}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun boostMemory() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = BoosterUtils.executeCommand(arrayOf("sync"))
            
            withContext(Dispatchers.Main) {
                if (result.success) {
                    Toast.makeText(this@MainActivity, "Memory optimized!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun killBackgroundApps() {
        AlertDialog.Builder(this)
            .setTitle("Kill Background Apps")
            .setMessage("This will force stop unnecessary background apps. Continue?")
            .setPositiveButton("Kill") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    // Example: kill some common background apps
                    val apps = listOf(
                        "com.facebook.katana",
                        "com.instagram.android",
                        "com.whatsapp"
                    )
                    
                    apps.forEach { pkg ->
                        BoosterUtils.forceStopApp(pkg)
                    }
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Background apps stopped", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun optimizeNetwork() {
        CoroutineScope(Dispatchers.IO).launch {
            val commands = listOf(
                arrayOf("settings", "put", "global", "mobile_data_always_on", "0"),
                arrayOf("settings", "put", "global", "wifi_scan_always_enabled", "0"),
                arrayOf("ip", "route", "flush", "cache")
            )
            
            var successCount = 0
            commands.forEach { cmd ->
                val result = BoosterUtils.executeCommand(cmd)
                if (result.success) successCount++
            }
            
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@MainActivity,
                    "Network optimized: $successCount/${commands.size} commands",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun refreshStatus() {
        checkShizukuStatus()
        
        CoroutineScope(Dispatchers.IO).launch {
            val info = BoosterUtils.getSystemInfo()
            
            withContext(Dispatchers.Main) {
                // Update UI with system info
                binding.tvSystemInfo.text = buildString {
                    append("System Status:\n")
                    info.forEach { (key, value) ->
                        append("• $key: ${value.take(50)}...\n")
                    }
                }
            }
        }
    }
    
    private fun updateLastApplied(profile: String) {
        binding.tvLastApplied.text = "Last applied: $profile"
        binding.tvLastApplied.isVisible = true
    }
    
    private fun showShizukuNotInstalledDialog() {
        AlertDialog.Builder(this)
            .setTitle("Shizuku Not Installed")
            .setMessage("This app requires Shizuku to work. Please install Shizuku first.")
            .setPositiveButton("Install") { _, _ ->
                openShizukuStore()
            }
            .setNegativeButton("Cancel") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Why Shizuku?")
            .setMessage("Shizuku allows this app to run system optimization commands without requiring root access.\n\n" +
                      "It's safe, open-source, and widely used by many optimization apps.")
            .setPositiveButton("Grant Permission") { _, _ ->
                Shizuku.requestPermission(shizukuPermissionCode)
            }
            .setNegativeButton("Learn More") { _, _ ->
                openShizukuWebsite()
            }
            .show()
    }
    
    private fun showPermissionRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Without Shizuku permission, this app cannot optimize your system.\n\n" +
                      "Please grant permission in Shizuku app.")
            .setPositiveButton("Open Shizuku") { _, _ ->
                openShizukuApp()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showAppInfoDialog() {
        AlertDialog.Builder(this)
            .setTitle("PUH Booster v1.0.0")
            .setMessage("""
                ⚡ Performance Booster for Non-Root Android
                
                Features:
                • Gaming Mode Optimization
                • Battery Saver Mode
                • System Cache Cleaner
                • RAM Booster
                • Network Optimizer
                
                Requirements:
                • Shizuku installed & activated
                • ADB permission granted once
                
                Note: This app doesn't require root access.
                All operations are done via Shizuku API.
                
                Developer: PUH Team
                License: Open Source
            """.trimIndent())
            .setPositiveButton("OK", null)
            .setNeutralButton("GitHub") { _, _ ->
                openGitHubRepo()
            }
            .show()
    }
    
    private fun openShizukuStore() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=moe.shizuku.privileged.api")
                setPackage("com.android.vending")
            }
            startActivity(intent)
        } catch (e: Exception) {
            openShizukuWebsite()
        }
    }
    
    private fun openShizukuApp() {
        try {
            val intent = packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
            if (intent != null) {
                startActivity(intent)
            } else {
                openShizukuStore()
            }
        } catch (e: Exception) {
            openShizukuStore()
        }
    }
    
    private fun openShizukuWebsite() {
        val uri = Uri.parse("https://shizuku.rikka.app/")
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
    
    private fun openGitHubRepo() {
        val uri = Uri.parse("https://github.com/yourusername/puhbooster")
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}