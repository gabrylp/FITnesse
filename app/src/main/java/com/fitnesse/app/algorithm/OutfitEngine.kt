package com.fitnesse.app.algorithm

import com.fitnesse.app.data.model.ClothingItem

data class SelectedOutfit(
    val top: ClothingItem?,
    val bottom: ClothingItem?,
    val outerwear: ClothingItem?,
    val footwear: ClothingItem?,
    val accessory: ClothingItem? = null,
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

data class ScoredOutfit(
    val top: ClothingItem?,
    val bottom: ClothingItem?,
    val outerwear: ClothingItem?,
    val footwear: ClothingItem?,
    val accessory: ClothingItem?,
    val score: Int,
    val reasoning: String,
)

fun selectBestOutfit(available: List<ClothingItem>): ScoredOutfit {
    val tops = available.filter { it.category.equals("top", ignoreCase = true) }
    val bottoms = available.filter { it.category.equals("bottom", ignoreCase = true) }
    val outerwear = available.filter { it.category.equals("outerwear", ignoreCase = true) }
    val footwear = available.filter { it.category.equals("footwear", ignoreCase = true) || it.category.equals("shoes", ignoreCase = true) }
    val accessories = available.filter { it.category.equals("accessory", ignoreCase = true) }
    val dresses = available.filter { it.category.equals("dress", ignoreCase = true) }

    var best: ScoredOutfit? = null

    fun consider(t: ClothingItem?, b: ClothingItem?, o: ClothingItem?, f: ClothingItem?, a: ClothingItem?, desc: String) {
        val score = scoreOutfit(t, b, o, f, a)
        if (best == null || score > best!!.score) {
            val reasons = buildReasons(t, b, o, f, a)
            best = ScoredOutfit(t, b, o, f, a, score, reasons)
        }
    }

    if (dresses.isNotEmpty()) {
        for (d in dresses) {
            if (outerwear.isEmpty() && footwear.isEmpty() && accessories.isEmpty()) {
                consider(d, null, null, null, null, "dress only")
            }
            for (o in outerwear + listOf(null)) {
                for (f in footwear + listOf(null)) {
                    for (a in accessories + listOf(null)) {
                        consider(d, null, o, f, a, "dress outfit")
                    }
                }
            }
        }
    }

    if (tops.isNotEmpty() && bottoms.isNotEmpty()) {
        for (t in tops) {
            for (b in bottoms) {
                if (outerwear.isEmpty() && footwear.isEmpty() && accessories.isEmpty()) {
                    consider(t, b, null, null, null, "minimal")
                }
                for (o in outerwear + listOf(null)) {
                    for (f in footwear + listOf(null)) {
                        for (a in accessories + listOf(null)) {
                            consider(t, b, o, f, a, "full")
                        }
                    }
                }
            }
        }
    }

    if (best == null && available.isNotEmpty()) {
        val items = available.take(4)
        best = ScoredOutfit(
            items.getOrNull(0), items.getOrNull(1), items.getOrNull(2),
            null, null, 0, "Selected first available items."
        )
    }

    return best ?: ScoredOutfit(null, null, null, null, null, 0, "No items available.")
}

private val lightColors = setOf(
    "white", "cream", "beige", "light yellow", "light gray", "light grey",
    "light pink", "light red", "light orange", "light green", "light blue",
    "light purple", "light brown", "coral", "olive",
)

private val darkColors = setOf(
    "black", "navy", "maroon", "dark red", "dark orange", "dark yellow",
    "dark green", "dark blue", "dark purple", "dark pink", "dark brown",
    "dark gray", "dark grey",
)

private fun colorLightness(name: String): String {
    val n = name.lowercase().trim()
    return when {
        n in lightColors -> "light"
        n in darkColors -> "dark"
        else -> "medium"
    }
}

private val complementaryPairs = setOf(
    "red" to "green", "green" to "red",
    "blue" to "orange", "orange" to "blue",
    "yellow" to "purple", "purple" to "yellow",
    "pink" to "dark green", "dark green" to "pink",
    "light blue" to "brown", "brown" to "light blue",
    "black" to "white", "white" to "black",
    "navy" to "beige", "beige" to "navy",
    "olive" to "maroon", "maroon" to "olive",
    "coral" to "teal", "teal" to "coral",
)

private fun extractBaseColor(name: String): String {
    val n = name.lowercase().trim()
    return n.removePrefix("light ").removePrefix("dark ")
}

private fun isComplementary(a: String, b: String): Boolean {
    val baseA = extractBaseColor(a)
    val baseB = extractBaseColor(b)
    return complementaryPairs.contains(baseA to baseB)
}

private fun scoreOutfit(top: ClothingItem?, bottom: ClothingItem?, outerwear: ClothingItem?, footwear: ClothingItem?, accessory: ClothingItem?): Int {
    var score = 0
    val layers = listOfNotNull(top, outerwear, bottom, footwear).map { it.dominantColor }
    val allItems = listOfNotNull(top, bottom, outerwear, footwear, accessory)

    if (top != null && bottom != null) score += 5

    val lightnesses = allItems.map { colorLightness(it.dominantColor) }
    for (i in 0 until lightnesses.size - 1) {
        if (lightnesses[i] == "light" && lightnesses[i + 1] == "dark") score += 3
        if (lightnesses[i] == "dark" && lightnesses[i + 1] == "light") score += 3
    }
    if (lightnesses.size >= 3) {
        if (lightnesses[0] == "light" && lightnesses[lightnesses.size - 1] == "light") score += 2
        if (lightnesses[0] == "dark" && lightnesses[lightnesses.size - 1] == "dark") score += 2
    }

    val colors = allItems.map { it.dominantColor }
    for (i in colors.indices) {
        for (j in i + 1 until colors.size) {
            if (isComplementary(colors[i], colors[j])) score += 3
        }
    }

    val patterns = allItems.map { it.pattern.lowercase() }
    val solidCount = patterns.count { it == "plain" || it == "solid" || it.isEmpty() }
    val patternedCount = patterns.size - solidCount
    if (patternedCount <= 1) score += 2
    val uniquePatterns = patterns.filter { it.isNotEmpty() && it != "plain" && it != "solid" }.distinct().size
    if (patternedCount >= 2 && uniquePatterns == patternedCount) score += 1

    allItems.forEach { item ->
        if (item.lastWorn > 0) {
            val daysSince = (System.currentTimeMillis() - item.lastWorn) / (24 * 60 * 60 * 1000)
            score += minOf(daysSince.toInt(), 3)
        }
    }

    return score
}

private fun buildReasons(top: ClothingItem?, bottom: ClothingItem?, outerwear: ClothingItem?, footwear: ClothingItem?, accessory: ClothingItem?): String {
    val parts = mutableListOf<String>()

    val allItems = listOfNotNull(top, bottom, outerwear, footwear, accessory)

    if (allItems.size >= 2) {
        val lightnesses = allItems.map { colorLightness(it.dominantColor) }
        if (lightnesses.any { it == "light" } && lightnesses.any { it == "dark" }) {
            parts.add("Sandwich method: alternates light and dark layers for depth.")
        }
    }

    val colors = allItems.map { it.dominantColor }
    val comps = mutableListOf<Pair<String, String>>()
    for (i in colors.indices) {
        for (j in i + 1 until colors.size) {
            if (isComplementary(colors[i], colors[j])) {
                comps.add(colors[i] to colors[j])
            }
        }
    }
    if (comps.isNotEmpty()) {
        val pair = comps.first()
        parts.add("Color theory: ${pair.first} and ${pair.second} complement each other.")
    }

    if (allItems.size >= 3) parts.add("Rule of thirds: layered silhouette creates visual balance.")

    if (parts.isEmpty()) parts.add("Selected based on availability and category variety.")

    return parts.joinToString(" ")
}

fun buildOutfitContext(available: List<ClothingItem>): String {
    val tops = available.filter { it.category.equals("top", ignoreCase = true) }
    val bottoms = available.filter { it.category.equals("bottom", ignoreCase = true) }
    val outerwear = available.filter { it.category.equals("outerwear", ignoreCase = true) }
    val footwear = available.filter { it.category.equals("footwear", ignoreCase = true) || it.category.equals("shoes", ignoreCase = true) }
    val accessories = available.filter { it.category.equals("accessory", ignoreCase = true) }
    val dresses = available.filter { it.category.equals("dress", ignoreCase = true) }

    fun format(item: ClothingItem) = "${item.id}: ${item.subcategory} (${item.dominantColor}, ${item.pattern})"

    return buildString {
        appendLine("Available tops: ${tops.joinToString { format(it) }}")
        appendLine("Available bottoms: ${bottoms.joinToString { format(it) }}")
        appendLine("Available outerwear: ${outerwear.joinToString { format(it) }}")
        appendLine("Available footwear: ${footwear.joinToString { format(it) }}")
        appendLine("Available accessories: ${accessories.joinToString { format(it) }}")
        if (dresses.isNotEmpty()) {
            appendLine("Available dresses: ${dresses.joinToString { format(it) }}")
        }
        appendLine("Styling rules: sandwich method (light/dark/light), color theory, rule of thirds.")
        appendLine("Return a JSON object with key 'selected' mapping each category to the item's id string (the part before the colon), and key 'reasoning' with your explanation.")
        appendLine("Pick one complete outfit and explain why.")
    }
}
