package com.puh.booster

import android.content.Context
import android.util.Log
import dev.rikka.shizuku.Shizuku
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object BoosterUtils {
    
    private const val TAG = "PUHBooster"
    
    enum class ProfileType {
        GAMING, BATTERY, BALANCED, CUSTOM
    }
    
    data class CommandResult(
        val success: Boolean,
        val output: String,
        val error: String = ""
    )
    
    /**
     * Execute command via Shizuku
     */
    suspend fun executeCommand(
        command: Array<String>,
        waitForCompletion: Boolean = true
    ): CommandResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val process = Shizuku.newProcess(command, null, null)
            
            val outputReader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            
            val output = StringBuilder()
            val error = StringBuilder()
            
            // Read output in background
            val outputJob = kotlinx.coroutines.launch {
                outputReader.useLines { lines ->
                    lines.forEach { output.append(it).append("\n") }
                }
            }
            
            // Read error in background
            val errorJob = kotlinx.coroutines.launch {
                errorReader.useLines { lines ->
                    lines.forEach { error.append(it).append("\n") }
                }
            }
            
            // Wait if required
            val exitCode = if (waitForCompletion) {
                process.waitFor()
            } else {
                0
            }
            
            // Wait for readers to finish
            outputJob.join()
            errorJob.join()
            
            val success = exitCode == 0
            CommandResult(success, output.toString(), error.toString())
            
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception: ${e.message}")
            CommandResult(false, "", "Permission denied: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Command execution failed: ${e.message}", e)
            CommandResult(false, "", "Execution failed: ${e.message}")
        }
    }
    
    /**
     * Apply performance profile
     */
    suspend fun applyProfile(profile: ProfileType, context: Context): Boolean {
        return when (profile) {
            ProfileType.GAMING -> applyGamingProfile(context)
            ProfileType.BATTERY -> applyBatteryProfile(context)
            ProfileType.BALANCED -> applyBalancedProfile(context)
            ProfileType.CUSTOM -> applyCustomProfile(context)
        }
    }
    
    private suspend fun applyGamingProfile(context: Context): Boolean {
        val commands = listOf(
            // Disable animations
            arrayOf("settings", "put", "global", "window_animation_scale", "0.0"),
            arrayOf("settings", "put", "global", "transition_animation_scale", "0.0"),
            arrayOf("settings", "put", "global", "animator_duration_scale", "0.0"),
            
            // Force GPU rendering
            arrayOf("settings", "put", "global", "force_gpu_rasterization", "1"),
            arrayOf("settings", "put", "global", "enable_gpu_debug_layers", "0"),
            
            // Disable battery optimization for games
            arrayOf("settings", "put", "global", "app_standby_enabled", "0"),
            
            // Increase touch sensitivity
            arrayOf("settings", "put", "system", "touch_sensitivity", "1"),
            
            // Disable adaptive brightness
            arrayOf("settings", "put", "system", "screen_brightness_mode", "0"),
        )
        
        return executeCommands(commands)
    }
    
    private suspend fun applyBatteryProfile(context: Context): Boolean {
        val commands = listOf(
            // Enable battery saver
            arrayOf("settings", "put", "global", "low_power", "1"),
            arrayOf("settings", "put", "global", "low_power_sticky", "1"),
            
            // Reduce background processes
            arrayOf("settings", "put", "global", "background_process_limit", "2"),
            
            // Disable animations
            arrayOf("settings", "put", "global", "window_animation_scale", "0.5"),
            arrayOf("settings", "put", "global", "transition_animation_scale", "0.5"),
            arrayOf("settings", "put", "global", "animator_duration_scale", "0.5"),
            
            // Limit location services
            arrayOf("settings", "put", "secure", "location_mode", "0"),
            
            // Disable auto-sync
            arrayOf("content", "query", "--uri", "content://sync/periodic"),
        )
        
        return executeCommands(commands)
    }
    
    private suspend fun applyBalancedProfile(context: Context): Boolean {
        val commands = listOf(
            // Reset to defaults
            arrayOf("settings", "put", "global", "window_animation_scale", "1.0"),
            arrayOf("settings", "put", "global", "transition_animation_scale", "1.0"),
            arrayOf("settings", "put", "global", "animator_duration_scale", "1.0"),
            arrayOf("settings", "put", "global", "low_power", "0"),
            arrayOf("settings", "put", "global", "force_gpu_rasterization", "0"),
            arrayOf("settings", "put", "system", "touch_sensitivity", "0"),
        )
        
        return executeCommands(commands)
    }
    
    private suspend fun applyCustomProfile(context: Context): Boolean {
        // Load custom commands from preferences
        val prefs = context.getSharedPreferences("booster_prefs", Context.MODE_PRIVATE)
        val customCommands = prefs.getStringSet("custom_commands", emptySet()) ?: emptySet()
        
        val commands = customCommands.map { command ->
            command.split(" ").toTypedArray()
        }
        
        return executeCommands(commands)
    }
    
    /**
     * Execute multiple commands
     */
    private suspend fun executeCommands(commands: List<Array<String>>): Boolean {
        var successCount = 0
        
        commands.forEach { cmd ->
            val result = executeCommand(cmd)
            if (result.success) {
                successCount++
                Log.d(TAG, "Success: ${cmd.joinToString(" ")}")
            } else {
                Log.w(TAG, "Failed: ${cmd.joinToString(" ")} - ${result.error}")
            }
        }
        
        return successCount > 0
    }
    
    /**
     * Clear app cache
     */
    suspend fun clearAppCache(packageName: String): CommandResult {
        return executeCommand(arrayOf("pm", "clear", packageName))
    }
    
    /**
     * Force stop app
     */
    suspend fun forceStopApp(packageName: String): CommandResult {
        return executeCommand(arrayOf("am", "force-stop", packageName))
    }
    
    /**
     * Get system information
     */
    suspend fun getSystemInfo(): Map<String, String> {
        val info = mutableMapOf<String, String>()
        
        // CPU info
        executeCommand(arrayOf("cat", "/proc/cpuinfo")).output
            .takeIf { it.isNotBlank() }
            ?.let { info["cpu"] = it }
        
        // Memory info
        executeCommand(arrayOf("cat", "/proc/meminfo")).output
            .takeIf { it.isNotBlank() }
            ?.let { info["memory"] = it }
        
        // Battery info
        executeCommand(arrayOf("dumpsys", "battery")).output
            .takeIf { it.isNotBlank() }
            ?.let { info["battery"] = it }
        
        return info
    }
    
    /**
     * Check if Shizuku is available
     */
    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder() && Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }
}