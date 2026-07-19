package com.fitnesse.app.data.model

data class ClothingItem(
    val id: String = "",
    val category: String = "",
    val subcategory: String = "",
    val dominantColor: String = "",
    val secondaryColor: String = "",
    val pattern: String = "",
    val length: String = "",
    val formality: String = "",
    val season: String = "",
    val photoURL: String = "",
    val lastWorn: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis(),
)

fun ClothingItem.isAvailable(cooldownDays: Int): Boolean {
    if (lastWorn == 0L) return true
    val cooldownMs = cooldownDays * 24L * 60 * 60 * 1000
    return System.currentTimeMillis() - lastWorn > cooldownMs
}
