package com.fitnesse.app.data.model

data class OutfitRecommendation(
    val id: String = "",
    val date: String = "",
    val items: List<String> = emptyList(),
    val reasoning: String = "",
    val confirmedWorn: Boolean = false,
)
