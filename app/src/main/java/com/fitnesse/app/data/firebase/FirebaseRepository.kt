package com.fitnesse.app.data.firebase

import com.fitnesse.app.data.model.ClothingItem
import com.fitnesse.app.data.model.OutfitRecommendation
import com.fitnesse.app.data.model.UserSettings
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()

    private val userId: String
        get() = AuthRepository().currentUser?.uid ?: "anonymous"

    private fun itemsRef() = db.collection("users").document(userId).collection("clothingItems")
    private fun outfitsRef() = db.collection("users").document(userId).collection("outfits")
    private fun settingsRef() = db.collection("users").document(userId).collection("settings").document("userSettings")

    suspend fun getClothingItems(): List<ClothingItem> {
        return try {
            itemsRef().get().await().documents.mapNotNull { it.toObject<ClothingItem>()?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addClothingItem(item: ClothingItem): String {
        val docRef = itemsRef().add(item).await()
        return docRef.id
    }

    suspend fun updateClothingItem(item: ClothingItem) {
        itemsRef().document(item.id).set(item).await()
    }

    suspend fun deleteClothingItem(id: String) {
        itemsRef().document(id).delete().await()
    }

    suspend fun getClothingItem(id: String): ClothingItem? {
        return try {
            val doc = itemsRef().document(id).get().await()
            doc.toObject<ClothingItem>()?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteOutfit(id: String) {
        outfitsRef().document(id).delete().await()
    }

    suspend fun getTodaysOutfit(date: String): OutfitRecommendation? {
        return try {
            val snapshot = outfitsRef().whereEqualTo("date", date).get().await()
            snapshot.documents.firstOrNull()?.toObject<OutfitRecommendation>()?.copy(id = snapshot.documents.first().id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveOutfit(outfit: OutfitRecommendation) {
        if (outfit.id.isEmpty()) {
            outfitsRef().add(outfit).await()
        } else {
            outfitsRef().document(outfit.id).set(outfit).await()
        }
    }

    suspend fun getOutfitHistory(): List<OutfitRecommendation> {
        return try {
            outfitsRef().orderBy("date").get().await().documents.mapNotNull { it.toObject<OutfitRecommendation>()?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserSettings(): UserSettings {
        return try {
            val doc = settingsRef().get().await()
            doc.toObject<UserSettings>() ?: UserSettings()
        } catch (e: Exception) {
            UserSettings()
        }
    }

    suspend fun saveUserSettings(settings: UserSettings) {
        settingsRef().set(settings).await()
    }

}
