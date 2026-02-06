package com.puh.booster.models

enum class ProfileType {
    GAMING,
    BALANCED,
    BATTERY,
    CUSTOM
}

data class PerformanceProfile(
    val type: ProfileType,
    val cpuMaxFreq: String = "",
    val gpuMaxFreq: String = "",
    val thermalProfile: String = "",
    val description: String = ""
)
