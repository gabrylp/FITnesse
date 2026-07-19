package com.fitnesse.app.ui.clothes

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnesse.app.data.model.ClothingItem
import com.fitnesse.app.data.repository.WardrobeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddClothesUiState(
    val imageUri: Uri? = null,
    val imageBytes: ByteArray? = null,
    val mimeType: String = "",
    val category: String = "",
    val subcategory: String = "",
    val dominantColor: String = "",
    val secondaryColor: String = "",
    val pattern: String = "",
    val length: String = "",
    val isAnalyzing: Boolean = false,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val error: String? = null,
    val savedSuccessfully: Boolean = false,
)

class AddClothesViewModel(
    private val repository: WardrobeRepository = WardrobeRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(AddClothesUiState())
    val state: StateFlow<AddClothesUiState> = _state.asStateFlow()

    fun setImage(uri: Uri, bytes: ByteArray, mimeType: String) {
        _state.value = _state.value.copy(
            imageUri = uri,
            imageBytes = bytes,
            mimeType = mimeType,
            isAnalyzing = true,
            isEditing = false,
            error = null,
        )
        analyzeImage(bytes, mimeType)
    }

    private fun analyzeImage(bytes: ByteArray, mimeType: String) {
        viewModelScope.launch {
            try {
                val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                val service = com.fitnesse.app.ai.GeminiService()
                val json = service.analyzeClothingImage(base64, mimeType)
                if (json.startsWith("Error:")) {
                    _state.value = _state.value.copy(
                        isAnalyzing = false,
                        error = json,
                    )
                    return@launch
                }
                val analysis = com.fitnesse.app.ai.GeminiAnalysisResult.fromJson(json)
                if (analysis.category.isBlank() && analysis.subcategory.isBlank()) {
                    _state.value = _state.value.copy(
                        isAnalyzing = false,
                        error = "Gemini couldn't identify this item. Try a clearer photo.",
                    )
                    return@launch
                }
                _state.value = _state.value.copy(
                    category = analysis.category,
                    subcategory = analysis.subcategory,
                    dominantColor = analysis.dominantColor,
                    secondaryColor = analysis.secondaryColor,
                    pattern = analysis.pattern,
                    length = analysis.length,
                    isAnalyzing = false,
                )
            } catch (e: Exception) {
                val detail = "${e::class.simpleName}: ${e.message ?: e.toString()}"
                android.util.Log.e("AddClothesVM", "analyzeImage failed", e)
                _state.value = _state.value.copy(
                    isAnalyzing = false,
                    error = "Analysis failed: $detail",
                )
            }
        }
    }

    fun retryAnalysis() {
        val bytes = _state.value.imageBytes ?: return
        val mimeType = _state.value.mimeType
        _state.value = _state.value.copy(error = null, isAnalyzing = true)
        analyzeImage(bytes, mimeType)
    }

    fun toggleEdit() {
        _state.value = _state.value.copy(isEditing = !_state.value.isEditing)
    }

    fun updateCategory(value: String) { _state.value = _state.value.copy(category = value) }
    fun updateSubcategory(value: String) { _state.value = _state.value.copy(subcategory = value) }
    fun updateDominantColor(value: String) { _state.value = _state.value.copy(dominantColor = value) }
    fun updateSecondaryColor(value: String) { _state.value = _state.value.copy(secondaryColor = value) }
    fun updatePattern(value: String) { _state.value = _state.value.copy(pattern = value) }
    fun updateLength(value: String) { _state.value = _state.value.copy(length = value) }

    fun save() {
        val s = _state.value
        val bytes = s.imageBytes ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            try {
                val photoUrl = repository.uploadPhoto(bytes)
                val item = ClothingItem(
                    category = s.category,
                    subcategory = s.subcategory,
                    dominantColor = s.dominantColor,
                    secondaryColor = s.secondaryColor,
                    pattern = s.pattern,
                    length = s.length,
                    photoURL = photoUrl,
                )
                repository.addClothingItem(item)
                _state.value = _state.value.copy(isSaving = false, savedSuccessfully = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save",
                )
            }
        }
    }
}
