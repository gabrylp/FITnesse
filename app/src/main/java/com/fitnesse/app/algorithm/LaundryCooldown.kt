package com.fitnesse.app.algorithm

import com.fitnesse.app.data.model.ClothingItem
import com.fitnesse.app.data.model.isAvailable

fun filterAvailable(
    items: List<ClothingItem>,
    cooldownDays: Int,
    cooldownEnabled: Boolean,
): List<ClothingItem> {
    if (!cooldownEnabled) return items
    return items.filter { it.isAvailable(cooldownDays) }
}
