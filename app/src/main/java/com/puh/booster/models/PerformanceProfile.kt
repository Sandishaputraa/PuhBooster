package com.puh.booster.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.puh.booster.R

/**
 * Data class representing a performance profile
 */
data class PerformanceProfile(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int,
    val colorRes: Int,
    val commands: List<Array<String>> = emptyList()
) {
    companion object {
        val GAMING = PerformanceProfile(
            id = "gaming",
            nameRes = R.string.profile_gaming,
            descriptionRes = R.string.profile_gaming_desc,
            iconRes = R.drawable.ic_gaming,
            colorRes = R.color.gaming_card,
            commands = listOf(
                arrayOf("settings", "put", "global", "window_animation_scale", "0.0"),
                arrayOf("settings", "put", "global", "transition_animation_scale", "0.0"),
                arrayOf("settings", "put", "global", "animator_duration_scale", "0.0"),
                arrayOf("settings", "put", "global", "force_gpu_rasterization", "1"),
                arrayOf("settings", "put", "global", "app_standby_enabled", "0")
            )
        )
        
        val BALANCED = PerformanceProfile(
            id = "balanced",
            nameRes = R.string.profile_balanced,
            descriptionRes = R.string.profile_balanced_desc,
            iconRes = R.drawable.ic_balanced,
            colorRes = R.color.balanced_card,
            commands = listOf(
                arrayOf("settings", "put", "global", "window_animation_scale", "1.0"),
                arrayOf("settings", "put", "global", "transition_animation_scale", "1.0"),
                arrayOf("settings", "put", "global", "animator_duration_scale", "1.0"),
                arrayOf("settings", "put", "global", "force_gpu_rasterization", "0"),
                arrayOf("settings", "put", "global", "low_power", "0")
            )
        )
        
        val BATTERY = PerformanceProfile(
            id = "battery",
            nameRes = R.string.profile_battery,
            descriptionRes = R.string.profile_battery_desc,
            iconRes = R.drawable.ic_battery,
            colorRes = R.color.battery_card,
            commands = listOf(
                arrayOf("settings", "put", "global", "low_power", "1"),
                arrayOf("settings", "put", "global", "window_animation_scale", "0.5"),
                arrayOf("settings", "put", "global", "transition_animation_scale", "0.5"),
                arrayOf("settings", "put", "global", "animator_duration_scale", "0.5"),
                arrayOf("settings", "put", "global", "background_process_limit", "2")
            )
        )
        
        val ALL_PROFILES = listOf(GAMING, BALANCED, BATTERY)
        
        fun fromId(id: String): PerformanceProfile? {
            return ALL_PROFILES.find { it.id == id }
        }
    }
}