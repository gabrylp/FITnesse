package com.fitnesse.app.ui.wardrobe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnesse.app.data.repository.WardrobeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddClothingUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
)

class AddClothingViewModel(
    private val repository: WardrobeRepository = WardrobeRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(AddClothingUiState())
    val state: StateFlow<AddClothingUiState> = _state.asStateFlow()

    fun addClothing(imageBytes: ByteArray, mimeType: String) {
        viewModelScope.launch {
            _state.value = AddClothingUiState(isLoading = true)
            val result = repository.analyzeAndAddClothing(imageBytes, mimeType)
            if (result.isSuccess) {
                _state.value = AddClothingUiState(isSuccess = true)
            } else {
                val error = result.exceptionOrNull()
                _state.value = AddClothingUiState(error = error?.message ?: error?.javaClass?.simpleName ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _state.value = AddClothingUiState()
    }
}
