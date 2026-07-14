package com.fitnesse.app.data.repository

import com.fitnesse.app.algorithm.OutfitSelectionResult
import com.fitnesse.app.algorithm.buildOutfitContext
import com.fitnesse.app.algorithm.filterAvailable
import com.fitnesse.app.data.firebase.AuthRepository
import com.fitnesse.app.data.firebase.FirebaseRepository
import com.fitnesse.app.data.model.ClothingItem
import com.fitnesse.app.data.model.OutfitRecommendation
import com.fitnesse.app.data.model.UserSettings
import com.fitnesse.app.ai.GeminiService
import com.fitnesse.app.ai.GeminiAnalysisResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WardrobeRepository(
    private val firebaseRepo: FirebaseRepository = FirebaseRepository(),
    private val authRepo: AuthRepository = AuthRepository(),
    private val geminiService: GeminiService? = GeminiService(),
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun getClothingItems(): List<ClothingItem> = firebaseRepo.getClothingItems()

    suspend fun addClothingItem(item: ClothingItem): String = firebaseRepo.addClothingItem(item)

    suspend fun analyzeAndAddClothing(imageBytes: ByteArray, mimeType: String): Result<String> {
        return try {
            val base64 = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
            val json = geminiService?.analyzeClothingImage(base64, mimeType) ?: return Result.failure(Exception("Gemini not configured"))
            val analysis = GeminiAnalysisResult.fromJson(json)
            val photoUrl = firebaseRepo.uploadPhoto(imageBytes)
            val item = ClothingItem(
                category = analysis.category,
                subcategory = analysis.subcategory,
                dominantColor = analysis.dominantColor,
                secondaryColor = analysis.secondaryColor,
                pattern = analysis.pattern,
                length = analysis.length,
                photoURL = photoUrl,
            )
            val id = firebaseRepo.addClothingItem(item)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTodaysOutfit(): OutfitRecommendation? {
        val today = dateFormat.format(Date())
        val saved = firebaseRepo.getTodaysOutfit(today)
        if (saved != null) return saved

        val settings = firebaseRepo.getUserSettings()
        val allItems = firebaseRepo.getClothingItems()
        val available = filterAvailable(allItems, settings.cooldownDays, settings.laundryCooldownEnabled)

        if (available.isEmpty()) return null

        return if (geminiService != null) {
            val context = buildOutfitContext(available)
            val response = geminiService.getOutfitRecommendation(context)
            val selection = OutfitSelectionResult.fromJson(response)
            val recommendedItemIds = selection.selected.values.filter { it.isNotEmpty() }

            val outfit = OutfitRecommendation(
                date = today,
                items = recommendedItemIds,
                reasoning = selection.reasoning,
            )
            firebaseRepo.saveOutfit(outfit)
            outfit
        } else {
            val fallback = OutfitRecommendation(
                date = today,
                items = available.take(2).map { it.id },
                reasoning = "Fallback: selected first available items (Gemini not configured).",
            )
            firebaseRepo.saveOutfit(fallback)
            fallback
        }
    }

    suspend fun confirmWorn(outfitId: String, itemIds: List<String>) {
        firebaseRepo.saveOutfit(
            OutfitRecommendation(
                id = outfitId,
                date = dateFormat.format(Date()),
                items = itemIds,
                confirmedWorn = true,
            )
        )
        val items = firebaseRepo.getClothingItems()
        items.filter { it.id in itemIds }.forEach {
            firebaseRepo.updateClothingItem(it.copy(lastWorn = System.currentTimeMillis()))
        }
    }

    suspend fun getOutfitHistory(): List<OutfitRecommendation> = firebaseRepo.getOutfitHistory()

    suspend fun getUserSettings(): UserSettings = firebaseRepo.getUserSettings()

    suspend fun saveUserSettings(settings: UserSettings) = firebaseRepo.saveUserSettings(settings)

    suspend fun signUp(email: String, password: String) = authRepo.signUp(email, password)

    suspend fun signIn(email: String, password: String) = authRepo.signIn(email, password)

    fun signOut() = authRepo.signOut()

    fun isSignedIn(): Boolean = authRepo.isSignedIn()
}
