package com.fitnesse.app.data.firebase

import com.fitnesse.app.data.model.ClothingItem
import com.fitnesse.app.data.model.OutfitRecommendation
import com.fitnesse.app.data.model.UserSettings
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

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
        item.id.let { itemsRef().document(it).set(item).await() }
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

    suspend fun uploadPhoto(byteArray: ByteArray): String = withContext(Dispatchers.IO) {
        val filename = "${UUID.randomUUID()}.jpg"
        val path = "users/$userId/photos/$filename"
        val ref = storage.reference.child(path)
        
        try {
            // Step 1: Perform the upload
            val uploadTask = ref.putBytes(byteArray).await()
            
            // Step 2: Verify the upload actually happened
            if (uploadTask.task.isSuccessful) {
                // Step 3: Get the download URL
                return@withContext ref.downloadUrl.await().toString()
            } else {
                throw Exception("Upload task not successful.")
            }
        } catch (e: Exception) {
            val technicalError = e.message ?: e.javaClass.simpleName
            throw Exception("Firebase Storage Error: $technicalError. \n\nCheck Firebase Console -> Storage -> Rules.")
        }
    }
}
