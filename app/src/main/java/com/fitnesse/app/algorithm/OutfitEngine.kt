package com.fitnesse.app.algorithm

import com.fitnesse.app.data.model.ClothingItem

data class SelectedOutfit(
    val top: ClothingItem?,
    val bottom: ClothingItem?,
    val outerwear: ClothingItem?,
    val footwear: ClothingItem?,
)

data class OutfitSelectionResult(
    val selected: Map<String, String>,
    val reasoning: String,
) {
    companion object {
        fun fromJson(json: String): OutfitSelectionResult {
            val regex = """"selected":\s*\{([^}]+)\}""".toRegex()
            val selectedMap = mutableMapOf<String, String>()
            val selectedMatch = regex.find(json)
            if (selectedMatch != null) {
                val pairs = selectedMatch.groupValues[1]
                val pairRegex = """["'](\w+)["']\s*:\s*["']([^"']*)["']""".toRegex()
                pairRegex.findAll(pairs).forEach { match ->
                    selectedMap[match.groupValues[1]] = match.groupValues[2]
                }
            }

            val reasonRegex = """"reasoning"\s*:\s*"([^"]*)"""".toRegex()
            val reasoning = reasonRegex.find(json)?.groupValues?.get(1) ?: ""

            return OutfitSelectionResult(
                selected = selectedMap,
                reasoning = reasoning,
            )
        }
    }
}

fun buildOutfitContext(available: List<ClothingItem>): String {
    val tops = available.filter { it.category == "top" }
    val bottoms = available.filter { it.category == "bottom" }
    val outerwear = available.filter { it.category == "outerwear" }
    val footwear = available.filter { it.category == "footwear" }

    return buildString {
        appendLine("Available tops: ${tops.joinToString { "${it.subcategory} (${it.dominantColor}, ${it.pattern})" }}")
        appendLine("Available bottoms: ${bottoms.joinToString { "${it.subcategory} (${it.dominantColor}, ${it.pattern})" }}")
        appendLine("Available outerwear: ${outerwear.joinToString { "${it.subcategory} (${it.dominantColor}, ${it.pattern})" }}")
        appendLine("Available footwear: ${footwear.joinToString { "${it.subcategory} (${it.dominantColor}, ${it.pattern})" }}")
        appendLine("Styling rules: sandwich method (light/dark/light), color theory, rule of thirds.")
        appendLine("Pick one complete outfit and explain why.")
    }
}
