package com.fitnesse.app.algorithm

import com.fitnesse.app.data.model.ClothingItem
import com.fitnesse.app.data.model.UserSettings
import com.fitnesse.app.data.model.isAvailable

fun filterAvailable(
    items: List<ClothingItem>,
    settings: UserSettings,
): List<ClothingItem> {
    if (!settings.laundryCooldownEnabled) return items
    return items.filter { it.isAvailable(settings) }
}
