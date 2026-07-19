package com.fitnesse.app.data.model

data class UserSettings(
    val laundryCooldownEnabled: Boolean = true,
    val cooldownDays: Int = 3,
    val cooldownCategories: List<String> = listOf("top", "bottom", "outerwear"),
    val theme: String = "system",
)
