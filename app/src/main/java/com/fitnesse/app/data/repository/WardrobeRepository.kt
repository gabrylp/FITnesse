package com.fitnesse.app.data.repository

import com.fitnesse.app.algorithm.OutfitSelectionResult
import com.fitnesse.app.algorithm.buildOutfitContext
import com.fitnesse.app.algorithm.filterAvailable
import com.fitnesse.app.algorithm.selectBestOutfit
import com.fitnesse.app.data.firebase.AuthRepository
import com.fitnesse.app.data.firebase.FirebaseRepository
import com.fitnesse.app.data.local.PhotoStorage
import com.fitnesse.app.data.model.ClothingItem
import com.fitnesse.app.data.model.OutfitRecommendation
import com.fitnesse.app.data.model.UserSettings
import com.fitnesse.app.ai.GeminiService
import com.fitnesse.app.ai.GeminiAnalysisResult
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WardrobeRepository(
    private val firebaseRepo: FirebaseRepository = FirebaseRepository(),
    private val authRepo: AuthRepository = AuthRepository(),
    private val geminiService: GeminiService? = GeminiService(),
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun getClothingItems(): List<ClothingItem> = firebaseRepo.getClothingItems()

    suspend fun getClothingItem(id: String): ClothingItem? = firebaseRepo.getClothingItem(id)

    suspend fun addClothingItem(item: ClothingItem): String = firebaseRepo.addClothingItem(item)

    suspend fun updateClothingItem(item: ClothingItem) = firebaseRepo.updateClothingItem(item)

    suspend fun deleteClothingItem(id: String) = firebaseRepo.deleteClothingItem(id)

    suspend fun deleteClothingItems(ids: List<String>) {
        ids.forEach { firebaseRepo.deleteClothingItem(it) }
    }

    suspend fun deleteOutfit(id: String) = firebaseRepo.deleteOutfit(id)

    suspend fun uploadPhoto(byteArray: ByteArray): String = PhotoStorage.savePhoto(byteArray)

    suspend fun analyzeAndAddClothing(imageBytes: ByteArray, mimeType: String): Result<String> {
        return try {
            val base64 = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
            val json = geminiService?.analyzeClothingImage(base64, mimeType) ?: return Result.failure(Exception("Gemini not configured"))
            val analysis = GeminiAnalysisResult.fromJson(json)
            val photoUrl = PhotoStorage.savePhoto(imageBytes)
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

    private suspend fun buildOutfit(available: List<ClothingItem>): OutfitRecommendation? {
        if (available.isEmpty()) return null

        val algorithmic = selectBestOutfit(available)
        val algoItems = listOfNotNull(algorithmic.top, algorithmic.bottom, algorithmic.outerwear, algorithmic.footwear, algorithmic.accessory)

        var itemIds = algoItems.map { it.id }
        var reasoning = algorithmic.reasoning

        if (geminiService != null && algoItems.isNotEmpty()) {
            val context = buildOutfitContext(available)
            val response = geminiService.getOutfitRecommendation(context)
            val selection = OutfitSelectionResult.fromJson(response)
            val geminiIds = selection.selected.values.filter { it.isNotEmpty() }
            if (geminiIds.isNotEmpty()) {
                val resolved = geminiIds.mapNotNull { id -> available.find { it.id == id } }
                if (resolved.isNotEmpty()) {
                    itemIds = resolved.map { it.id }
                    reasoning = selection.reasoning.ifEmpty { algorithmic.reasoning }
                }
            }
        }

        return OutfitRecommendation(
            items = itemIds,
            reasoning = reasoning,
        )
    }

    private suspend fun generateOutfit(available: List<ClothingItem>, today: String): OutfitRecommendation? {
        val outfit = buildOutfit(available)?.copy(date = today) ?: return null
        firebaseRepo.saveOutfit(outfit)
        return outfit
    }

    suspend fun generateOutfitPreview(dateStr: String): OutfitRecommendation? {
        val settings = firebaseRepo.getUserSettings()
        val allItems = firebaseRepo.getClothingItems()
        val available = filterAvailable(allItems, settings)
        return buildOutfit(available)?.copy(date = dateStr)
    }

    suspend fun getTodaysOutfit(): OutfitRecommendation? {
        val today = dateFormat.format(Date())
        val saved = firebaseRepo.getTodaysOutfit(today)
        if (saved != null && saved.items.isNotEmpty()) return saved

        val settings = firebaseRepo.getUserSettings()
        val allItems = firebaseRepo.getClothingItems()
        val available = filterAvailable(allItems, settings)
        return generateOutfit(available, today)
    }

    suspend fun regenerateOutfit(): OutfitRecommendation? {
        val today = dateFormat.format(Date())
        val existing = firebaseRepo.getTodaysOutfit(today)
        if (existing != null) firebaseRepo.deleteOutfit(existing.id)
        val settings = firebaseRepo.getUserSettings()
        val allItems = firebaseRepo.getClothingItems()
        val available = filterAvailable(allItems, settings)
        return generateOutfit(available, today)
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

    suspend fun getOutfitHistory(): List<OutfitRecommendation> {
        val all = firebaseRepo.getOutfitHistory()
        val cutoff = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.time
        val cutoffDate = dateFormat.format(cutoff)
        all.filter { it.date < cutoffDate }.forEach { firebaseRepo.deleteOutfit(it.id) }
        return all.filter { it.date >= cutoffDate }
    }

    suspend fun getUserSettings(): UserSettings = firebaseRepo.getUserSettings()

    suspend fun saveUserSettings(settings: UserSettings) = firebaseRepo.saveUserSettings(settings)

    suspend fun signUp(email: String, password: String) = authRepo.signUp(email, password)

    suspend fun signIn(email: String, password: String) = authRepo.signIn(email, password)

    fun signOut() = authRepo.signOut()

    fun isSignedIn(): Boolean = authRepo.isSignedIn()

    fun getCurrentUserEmail(): String = authRepo.currentUser?.email ?: ""

    suspend fun sendPasswordReset(email: String): Result<Unit> = authRepo.sendPasswordReset(email)

    suspend fun resolveOutfitItems(itemIds: List<String>): List<ClothingItem> {
        val allItems = firebaseRepo.getClothingItems()
        return itemIds.mapNotNull { id -> allItems.find { it.id == id } }
    }
}
