package com.fitnesse.app.data.model

data class UserSettings(
    val laundryCooldownEnabled: Boolean = true,
    val cooldownDays: Int = 3,
    val theme: String = "system",
)
